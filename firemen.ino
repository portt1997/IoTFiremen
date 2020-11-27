#include <SimpleTimer.h>
#include <SoftwareSerial.h>
#include <EEPROM.h>
#include <avr/pgmspace.h>
#include <Wire.h>
#include <Stepper.h>
#include <OneWire.h>
#include <Adafruit_AMG88xx.h>


#define AT_FLAG 0
#define AT_ID 1
#define AT_FIRE 40
#define AT_BAUD_SET 42

#define AT_SSID 50
#define AT_PW 80

#define AT_IP 110
#define AT_PORT 140


SoftwareSerial mySerial(2, 3); // RX, TX

char strbuf[40];
int intBuf;
String stringBuf;
double doubleBuf;
String Serialbuffer;

float pixels[AMG88xx_PIXEL_ARRAY_SIZE];
Adafruit_AMG88xx amg;
float temps[2]= {30};
int Twarn = 0, Gwarn = 0;

int gasPin = 0; //analog pin 0
int DS18S20=4;  //digital pin 4
OneWire ds(DS18S20);

int WIFIdisconnected = 1;
int IPdisconnected = 0;
SimpleTimer aliveTimer;
SimpleTimer compareTimer;

float temp = 0.0;
float gas = 0.0;

String tempSSID = "";
String tempPW = "";

unsigned long longbuf;

bool resettingDevice = false;
int strLength = 0;

void setup() {
  pinMode(A0, INPUT);
  Serial.begin(9600);
  Wire.begin();
  aliveTimer.setInterval(900000); //wifi check timer setting 15min
  compareTimer.setInterval(20000);  //compare and check temp and gas each 20sec;
  for(int i=0;i<300;i++)  // for test
    EEPROM.write(i,0);
  if (!amg.begin()) {
    Serial.println("Could not find a valid AMG88xx sensor, check wiring!");
    //while (1);
    
  }
  else{
    Serial.println("AMG Sensor boot complete");
  }

  


  if(EEPROM.read(AT_FLAG) != 1)
    EEPInitializing();
    if(EEPROM.read(AT_FIRE) != 1)
    EEPROM.write(AT_FIRE,0);
  
  // set the data rate for the SoftwareSerial port
  if(!EEPROM.read(AT_BAUD_SET)){
      mySerial.begin(115200);
      mySerial.print(F("AT+UART_DEF=9600,8,1,0,0\r\n"));
      mySerial.find("OK");
      mySerial.print(F("AT+RST\r\n"));
      mySerial.find("OK");
      mySerial.begin(9600);
      EEPROM.write(AT_BAUD_SET, 1);
      //EEPROM.commit();
  }
  else
    mySerial.begin(9600);
  
  mySerial.print(F("AT+CWMODE=1\r\n"));
  if(mySerial.find("OK")){
    Serial.println(F("CWMODE 1"));
  }
  mySerial.print(F("AT+CIPMODE=0\r\n"));
  if(mySerial.find("OK")){
    Serial.println(F("CIPMODE 0"));
  }
  mySerial.print(F("AT+CIPMUX=0\r\n"));
  if(mySerial.find("OK")){
    Serial.println(F("CIPMUX 0"));
  }
  if(EEPROM.read(AT_FLAG) == 1){
    getTemp();
    Serial.print("Temp Measured : ");
    Serial.println(temp);
    getGas();
    Serial.print("Gas Measured : ");
    Serial.println(gas);
    WIFIcheckAlive();
    getTemp();
    Serial.print("Temp Measured : ");
    Serial.println(temp);
    getGas();
    Serial.print("Gas Measured : ");
    Serial.println(gas);
  }
}
void loop(){
  String sendStringA = "";
  float prevTemp;
  float prevGas;
    if(aliveTimer.isReady()){
      WIFIcheckAlive();
      aliveTimer.reset();
    }
    if(compareTimer.isReady()){
      WIFIcheckAlive();
      prevTemp = getTemp();
      Serial.print("Temp Measured : ");
      Serial.println(temp);
      prevGas = getGas();
      Serial.print("Gas Measured : ");
      Serial.println(gas);
      


      if(!EEPROM.read(AT_FIRE)){
        sendATString("TEST/"+String(temp) + "/"+String(gas)); //for debugging
      }
      if(((prevTemp + 4.0) <= temp || (prevGas + 30.0) <= gas || temp >= 40.0||gas>=400)&& !EEPROM.read(AT_FIRE)){
        if(amgSensor()){
          EEPROM.write(AT_FIRE, 1);
          String message = "HW/";
          read_String(AT_ID,strbuf); 
          message += String(strbuf) + "/FIRE";
          Serial.println(F("sending Fire Request..."));
          sendATString(message); 
        }
      }
      compareTimer.reset();

    
    }
      
    if(mySerial.available()) {
      Serialbuffer = mySerial.readStringUntil('\n');
      receiveData();
    }

}

void receiveData(){
  if(-1 != Serialbuffer.indexOf("SERVER PIX")){
    strLength = 0;
      amgSensor();
      
      strLength=0;
    String msg = "HW/";
    read_String(AT_ID,strbuf); 
    msg += String(strbuf) + "/PIX";
    strLength +=msg.length();
    for(int i=0; i<AMG88xx_PIXEL_ARRAY_SIZE; i++){
      msg += "/" + String(pixels[i]);
      strLength+=1;
      strLength +=String(pixels[i]).length();
    }
    Serial.println(F("sending PIXELS..."));
      mySerial.print("\n");
    mySerial.print(F("AT+CIPSEND="));
    mySerial.print(strLength);
    mySerial.print(F("\r\n"));
    if(mySerial.find(">")){
      mySerial.print(msg);
    }
    if(mySerial.find("SEND OK")){
      Serial.println(F("SEND OK"));
    }
    else{
      Serial.println(F("SEND FAILED"));
    }
    EEPROM.write(AT_FIRE,1);
    Serial.println("GOT FIRE FROM THIS BUILDING");
  }

  else if(-1 != Serialbuffer.indexOf("SERVER FIRE CANCELLED")){
    EEPROM.write(AT_FIRE,0);
    Serial.println("FIRE CALL CANCELLED");
    getTemp();
    Serial.print("Temp Measured : ");
    Serial.println(temp);
    getGas();
    Serial.print("Gas Measured : ");
    Serial.println(gas);
    compareTimer.reset();
  }
}
int amgSensor(){
  int fire = 0;
  amg.readPixels(pixels);
  Serial.print("[");
  for(int i=1; i<=AMG88xx_PIXEL_ARRAY_SIZE; i++){
    if(pixels[i-1] > 40)
      fire++;
    Serial.print(pixels[i-1]);
    Serial.print(", ");
    if( i%8 == 0 ) Serial.println();
  }
  Serial.println("]");
  if(fire>1)
    return 1;
   else
    return 0;
}


void EEPInitializing(){
    if(EEPROM.read(AT_FLAG) != 1){
      Serial.println(F("initializing EEPROM..."));
      EEPROM.write(AT_FLAG,1);
      write_String(AT_ID,"123456");
      EEPROM.write(AT_BAUD_SET,0);
      EEPROM.write(AT_FIRE,0);
      write_String(AT_SSID,"AndroidHotspot6897");
      write_String(AT_PW,"00000011");
      
      write_String(AT_IP,"54.221.152.48");
      EEPROM.put(AT_PORT,(int)4000);
      read_String(AT_IP,strbuf);
      
      EEPROM.get(AT_PORT,intBuf);
      Serial.print(F("IP : "));
      Serial.println(strbuf);
      Serial.print(F("PORT : "));
      Serial.println(intBuf);
      Serial.println(F("initializing EEPROM complete!"));
  }
  else
    Serial.println(F("initializing EEPROM ALREADY complete!"));
}

void write_String(char address,const char data[])
{
  int _size = strlen(data);
  int i;
  for(i=0;i<_size;i++)
  {
    EEPROM.write(address+i,data[i]);
  }
  EEPROM.write(address+_size,'\0');
}

void read_String(char address, char* data)
{
  int i;
  int len=0;
  unsigned char k;
  k=EEPROM.read(address);
  while(k != '\0' && len<500)
  {    
    k=EEPROM.read(address+len);
    data[len]=k;
    len++;
  }
  data[len]='\0';
}

void WIFIcheckAlive(){
  Serial.println(F("WIFIchecking..."));
  Serial.flush();
  mySerial.print(F("at+CWJAP?\r\n"));
  if(mySerial.find("No AP")){
    Serial.println(F("wifiDisconnected while checking WIFI STATUS"));
    reconnectWifi();
  }
  else{
    Serial.println(F("wifi Stable"));
    Serial.flush();
    IPcheck();
  }
}
void IPcheck(){
  Serial.println(F("IPchecking..."));
  mySerial.print(F("AT+CIPSTATUS\r\n"));
  if(mySerial.find("+CIPSTATUS:0")){
    Serial.println(F("IP Connected"));
    Serial.flush();
  }
  else{
    Serial.println(F("IP Disconnected while checking IP STATUS"));
    reconnectIP();
  }
}

void reconnectWifi(){
    mySerial.setTimeout(7000);
    Serial.println(F("WIFIconnecting..."));
    Serial.flush();
    mySerial.print(F("AT+CWJAP=\""));
    read_String(AT_SSID,strbuf);
    Serial.println(strbuf);
    mySerial.print(strbuf);
    mySerial.print(F("\",\""));
    read_String(AT_PW,strbuf);
    Serial.println(strbuf);
    mySerial.print(strbuf);
    mySerial.print(F("\"\r\n"));
    if(mySerial.find("OK"))
      Serial.println(F("WIFI connected"));
    else{
      Serial.println(F("Cannot Connect WIFI"));
    }
    mySerial.setTimeout(1000);
    reconnectIP();
}
void reconnectIP(){
    Serial.println(F("IPconnecting..."));
    mySerial.print(F("AT+CIPSTART=\"TCP\",\""));
    read_String(AT_IP,strbuf);
    mySerial.print(strbuf);
    mySerial.print(F("\","));
    EEPROM.get(AT_PORT,intBuf);
    stringBuf = String(intBuf);
    stringBuf.toCharArray(strbuf,stringBuf.length()+1);
    mySerial.print(strbuf);
    mySerial.print(F("\r\n"));
    mySerial.setTimeout(5000);
    if(mySerial.find("OK")){
      Serial.println(F("IP connected"));
      mySerial.setTimeout(1000);
      sendJoin();
    }
    else
      Serial.println(F("Cannot Connect IP"));
    mySerial.setTimeout(1000);
}

void sendJoin(){
    String message = "HW/";

    read_String(AT_ID,strbuf); 
    message += String(strbuf) + "/START";
    Serial.println(F("sending Joining Request..."));
    sendATString(message); 
}

bool sendATString(String str){
  mySerial.print("\n");
  mySerial.print(F("AT+CIPSEND="));
  mySerial.print(String(str.length()));
  Serial.println(str.length());
  mySerial.setTimeout(2000);
  mySerial.print(F("\r\n"));
  if(mySerial.find(">")){
    mySerial.print(str);
  }
  if(mySerial.find("SEND OK")){
    Serial.println(F("SEND OK"));
    return true;
  }
  else{
    Serial.println(F("SEND FAILED"));
    return false;
  }
  mySerial.setTimeout(1000);
}

float getTemp(){
  byte data[12];
  byte addr[8];
  float prevTemp;

  prevTemp = temp;
  if ( !ds.search(addr)) {
    ds.reset_search();
    return -1000;
  }

  if ( OneWire::crc8( addr, 7) != addr[7]) {
    //Serial.println("CRC is not valid!");
    return -1000;
  }

  if ( addr[0] != 0x10 && addr[0] != 0x28) {
    //Serial.print("Device is not recognized");
    return -1000;
  }

  ds.reset();
  ds.select(addr);
  ds.write(0x44, 1);
  byte present = ds.reset();
  ds.select(addr);
  ds.write(0xBE);
  for (int i = 0; i < 9; i++)  {
    data[i] = ds.read();
  }

  ds.reset_search();
  byte MSB = data[1];
  byte LSB = data[0];
  float tempRead = ((MSB << 8) | LSB);
  float TemperatureSum = tempRead / 16;
  temp=TemperatureSum-4;
  return prevTemp;
}
float getGas(){
  float prevGas;
  prevGas = gas;
    gas = analogRead(gasPin);
    return prevGas;
}
