# -*- coding: utf-8 -*-
import socket, threading
import pymysql
import numpy as np
import math
from matplotlib import pyplot as plt
from scipy.interpolate import griddata
import cv2
import os
import time

server_connection_list = []
#database = pymysql.connect(
#    user='root', 
#    passwd='1234', 
#    host='127.0.0.1', 
#    db='firemen', 
#    charset='utf8'
#)
#cursor = database.cursor(pymysql.cursors.DictCursor)
detectionThread = []

def binder(client_socket, addr):

    print('Connected by', addr);
    connection = { 'id' : "",
                   'type' : "",
                   'buildingCode' : "",
                   'isFire' : False,
                   'socket' : client_socket
        }
    global server_connection_list
    #global cursor
    database = pymysql.connect(
            user = 'root',
            passwd='1234',
            host = '127.0.0.1',
            db = 'firemen',
            charset='utf8'
            )
    cursor = database.cursor(pymysql.cursors.DictCursor)
    try:
    
        while True:
            data = client_socket.recv(2048);
            msg = data.decode();
            #print(msg)
            datas = msg.split('/');
            if datas[0] == 'TEST':
                print('온도 : ',datas[1])
                print('연기 : ',datas[2])
            if datas[0] == 'HW':
                print(msg)
                if datas[2] == 'START':
                    sql = '''SELECT buildingCode from hw'''
                    cursor.execute(sql)
                    result = cursor.fetchone()
                    if result != None:
                        connection['buildingCode'] = result['buildingCode']
                        connection['id'] = datas[1]
                        connection['type'] = 'hw'
                        checkID(connection) #connection append
                elif datas[2] == 'FIRE' and connection['isFire'] == False:
                    for con in server_connection_list:
                        if con['buildingCode'] == connection['buildingCode'] and con['type'] == 'hw':
                            con['isFire'] = True
                    global detectionThread
                    th = threading.Thread(target=fireOccurred, kwargs = {'BC' : connection['buildingCode']})
                    th.start();
                    singleDetectionThread = { 'buildingCode' : connection['buildingCode'],
                                              'thread' : th,
                                              'hwID' : datas[1],
                                              'Fire' : True
                        }
                    detectionThread.append(singleDetectionThread);
                    sql = '''update register set isFire = 1 where buildingCode = %s'''
                    cursor.execute(sql,(connection['buildingCode']))
                    database.commit()
                    sql = '''update hw set isFire = 1 where HWID = %s'''
                    cursor.execute(sql,(connection['id']))
                    database.commit()
                    sql = '''select * from hw where HWID = %s'''
                    cursor.execute(sql,(connection['id']))
                    result = cursor.fetchone()
                    sql = '''select * from register where buildingCode = %s'''
                    cursor.execute(sql,(connection['buildingCode']))
                    rs = cursor.fetchall()
                    for con in server_connection_list:
                        #print('con check')
                        for r in rs:
                            #print('db check')
                            #print(con['id'])
                            #print(r['app'])
                            #print(con['type'])
                            if r['app'] == con['id'] and con['type'] == 'app':
                                #print('send ready')
                                try:
                                    placeMSG = 'SERVER/FIRE/'+result['address']+'/'+str(result['hosu']) + ' ' + result['place']
                                    con['socket'].sendall(placeMSG.encode())
                                    #print('app send OK')
                                    break
                                except:
                                    print('APP sendFail')
                elif datas[2] == 'PIX':
                    print('MK IMG')
                    pixels = []
                    for i in range(3,67):
                        pixels.append(float(datas[i]))
                    pixmax = max(pixels)
                    pixels = [x / pixmax for x in pixels]
                    points = [(math.floor(ix / 8), (ix % 8)) for ix in range(0, 64)]
                    grid_x, grid_y = np.mgrid[0:7:32j, 0:7:32j]

                    # bicubic interpolation of 8x8 grid to make a 32x32 grid
                    bicubic = griddata(points, pixels, (grid_x, grid_y), method='cubic')
                    image = np.array(bicubic)
                    image = np.reshape(image, (32, 32))
                    try:
                        plt.imsave(connection['id']+'.jpg', image)
                    except Exception as msg:
                        print('imsave error')
                        print(msg)

                    # Read image
                    img = cv2.imread(connection['id']+'.jpg', cv2.IMREAD_GRAYSCALE)
                    img = cv2.bitwise_not(img)

                    # Setup SimpleBlobDetector parameters.
                    params = cv2.SimpleBlobDetector_Params()
                    # Change thresholds
                    params.minThreshold = 10
                    params.maxThreshold = 255

                    # Filter by Area.
                    params.filterByArea = True
                    params.minArea = 5

                    # Filter by Circularity
                    params.filterByCircularity = True
                    params.minCircularity = 0.1

                    # Filter by Convexity
                    params.filterByConvexity = False
                    params.minConvexity = 0.87

                    # Filter by Inertia
                    params.filterByInertia = False
                    params.minInertiaRatio = 0.01

                    # Set up the detector with default parameters.
                    detector = cv2.SimpleBlobDetector_create(params)

                    # Detect blobs.
                    keypoints = detector.detect(img)

                    for i in range (0, len(keypoints)):
                            x = keypoints[i].pt[0]
                            y = keypoints[i].pt[1]

                    print(str(len(keypoints)))
                    sql = '''update hw set people = %s where HWID = %s'''
                    cursor.execute(sql,(str(len(keypoints)),connection['id']))
                    database.commit()


                
            elif 'APP' in datas[0]:
                print(msg)
                if datas[1] == 'JOIN':
                    sql = '''SELECT * from app;'''
                    cursor.execute(sql)
                    result = cursor.fetchall()
                    ava = True
                    for rs in result:
                        if datas[2] == rs['email']:
                            print('---join not available')
                            client_socket.sendall('SERVER/JOIN NOT AVAILABLE'.encode())
                            ava = False
                            break
                    if not ava:
                        print('---join available')
                        client_socket.sendall('SERVER/JOIN AVAILABLE'.encode())
                        sql = '''insert into app values(%s,%s,%s,%s,%s)'''
                        cursor.execute(sql, (datas[2],datas[3],datas[5],datas[4],0))
                        database.commit()
                    break
                    
                elif datas[1] == 'LOGIN':
                    sql = '''SELECT * from app where email = %s and pw = %s and role = 0'''
                    cursor.execute(sql, (datas[2],datas[3]))
                    result = cursor.fetchone()
                    if result == None:
                        client_socket.sendall('SERVER/LOGIN NOT AVAILABLE'.encode())
                        print('login not ava')
                    else:
                        client_socket.sendall('SERVER/LOGIN AVAILABLE'.encode())
                        #connection['id'] = datas[2]
                        #connection['type'] = 'app'
                        #checkID(connection)
                        print('login success')
                    break
                elif datas[1] == 'FLOGIN':
                    sql = '''SELECT * from app where email = %s and pw = %s and role = 1'''
                    cursor.execute(sql, (datas[2],datas[3]))
                    result = cursor.fetchone()
                    if result == None:
                        client_socket.sendall('SERVER/LOGIN NOT AVAILABLE'.encode())
                    else:
                        client_socket.sendall('SERVER/LOGIN AVAILABLE'.encode())
                        #connection['id'] = datas[2]
                        #connection['type'] = 'app'
                        #checkID(connection)
                    break
                elif datas[1] == 'RLOGIN':
                    sql = '''SELECT * from app where email = %s and pw = %s and role = 2'''
                    cursor.execute(sql, (datas[2],datas[3]))
                    result = cursor.fetchone()
                    if result == None:
                        client_socket.sendall('SERVER/LOGIN NOT AVAILABLE'.encode())
                    else:
                        client_socket.sendall('SERVER/LOGIN AVAILABLE'.encode())
                        #connection['id'] = datas[2]
                        #connection['type'] = 'app'
                        #checkID(connection)
                    break
                
                elif datas[1] == 'FHWLIST':
                    sql = '''SELECT * from hw where address = %s order by isFire desc, people desc,hosu'''
                    cursor.execute(sql, (datas[2]))
                    result = cursor.fetchall()
                    print('Fireman HWList start')
                    BS = ''
                    for rs in result:
                        isDisconnected = True
                        buildingMSG = str(rs['hosu'])  + ' ' + rs['place'] + '/' + str(rs['people']) + '/' 
                        if rs['isFire'] == 1:
                            buildingMSG += 'True/'
                        else:
                            buildingMSG += 'False/'
                        for con in server_connection_list:
                            if con['id'] == rs['HWID']:
                                isDisconnected = False
                                break
                        if isDisconnected:
                            buildingMSG += 'True~'
                        else:
                            buildingMSG += 'False~'
                        print(buildingMSG)
                        BS += buildingMSG
                        
                    client_socket.sendall(BS.encode())
                    break
                
                elif datas[1] == 'RHWLIST':
                    sql = '''SELECT * from hw where address = %s order by hosu'''
                    cursor.execute(sql, (datas[2]))
                    result = cursor.fetchall()
                    print('manager HWList start')
                    BS = ''
                    for rs in result:
                        isDisconnected = True
                        buildingMSG = str(rs['hosu']) + ' ' + rs['place'] +'~' 
                        for con in server_connection_list:
                            if con['id'] == rs['HWID']:
                                isDisconnected = False
                                break
                        if isDisconnected:
                            print(buildingMSG)
                            BS += buildingMSG
                        
                    client_socket.sendall(BS.encode())
                    break

                elif datas[1] == 'FIRECANCELLED':
                    sql = '''SELECT * from hw where address = %s'''
                    cursor.execute(sql, (datas[2]))
                    result = cursor.fetchone()
                    print('cancelled : ',result['buildingCode'])
                    sql = '''update hw set isFire = 0 where buildingCode = %s'''
                    cursor.execute(sql,(result['buildingCode']))
                    database.commit()
                    sql = '''update hw set people = 0 where buildingCode = %s'''
                    cursor.execute(sql,(result['buildingCode']))
                    database.commit()
                    sql = '''update register set isFire = 0 where buildingCode = %s'''
                    cursor.execute(sql,(result['buildingCode']))
                    database.commit()
                    try:
                        for t in detectionThread:
                            if t['buildingCode'] == result['buildingCode']:
                                t['Fire'] = False
                                break
                        for con in server_connection_list:
                            if con['type'] == 'hw' and con['buildingCode'] == result['buildingCode']:
                                con['isFire'] = False
                                try:
                                    con['socket'].sendall('SERVER FIRE CANCELLED \n'.encode())
                                    print('HW FIRE CANCELLED')
                                except:
                                    print('HW sendFail')
                    except Exception as msg:
                        print('HW HW')
                        print(msg)
                    break
                        
                
                else:
                    if datas[2] == 'START':
                        print('start ok')
                        connection['id'] = datas[1]
                        connection['type'] = 'app'
                        checkID(connection)
                        
                    elif datas[2] == 'GETBUILDINGCODE':
                        sql = '''SELECT * from hw where buildingCode = %s'''
                        cursor.execute(sql, (datas[3]))
                        result = cursor.fetchone()
                        if result != None:
                            sql = '''SELECT * from register where buildingCode = %s and app = %s'''
                            cursor.execute(sql, (datas[3], datas[1]))
                            r = cursor.fetchone()
                            if r !=None:
                                client_socket.sendall('SERVER/DUPLICATED ADDRESS'.encode())
                                continue
                            sql = '''insert into register values(%s,%s,%s)'''
                            cursor.execute(sql, (datas[1],datas[3],0))
                            database.commit()
                            availableMSG = 'SERVER/' + result['address'] + '/' + datas[3]
                            client_socket.sendall(availableMSG.encode())
                        else:
                            client_socket.sendall('SERVER/NOT AVAILABLE ADDRESS'.encode())
                        break
                            
                    elif datas[2] == 'DELETEBUILDINGCODE':
                        sql = '''select * from hw where address = %s'''
                        cursor.execute(sql, (datas[3]))
                        r = cursor.fetchone()
                        sql = '''delete from register where app = %s and buildingCode = %s'''
                        cursor.execute(sql, (datas[1],r['buildingCode']))
                        database.commit()
                        break
                    
                    elif datas[2] == 'BUILDINGLIST':
                        sql = '''SELECT * from register where app = %s order by isFire desc'''
                        cursor.execute(sql, (datas[1]))
                        result = cursor.fetchall()
                        print('buildingList start')
                        BS = ''
                        for rs in result:
                            sql = '''SELECT * from hw where buildingCode = %s'''
                            cursor.execute(sql, (rs['buildingCode']))
                            r = cursor.fetchone()
                            buildingMSG = '' + r['address'] + '/'
                            if rs['isFire'] == 1:
                                buildingMSG += 'True~'
                            else:
                                buildingMSG += 'False~'
                            print(buildingMSG)
                            BS += buildingMSG
                        
                        client_socket.sendall(BS.encode())
                        break

                    elif datas[2] == 'RBUILDINGLIST':
                        sql = '''SELECT * from register where app = %s'''
                        cursor.execute(sql, (datas[1]))
                        result = cursor.fetchall()
                        print('RbuildingList start')
                        BS = ''
                        for rs in result:
                            isDisconnected = False
                            sql = '''SELECT * from hw where buildingCode = %s'''
                            cursor.execute(sql, (rs['buildingCode']))
                            r = cursor.fetchone()
                            buildingMSG = r['address'] + '/True~'
                            for con in server_connection_list:
                                if con['type'] == 'hw' and con['id'] == r['HWID']:
                                    print('falsss')
                                else:
                                    isDisconnected = True
                                    break
                                    
                            if isDisconnected:
                                print(buildingMSG)
                                BS += buildingMSG
                        if BS == '':
                            client_socket.sendall('aa/False~'.encode())
                        else:
                            client_socket.sendall(BS.encode())
                        break
    except socket.error:
        print('connection error')
                    
    except Exception as msg:
        print("except : " , addr);
        print(msg)
    finally:
        if connection['id'] != "":
            try:
                server_connection_list.remove(connection)
                print("connection closed successfully")
            except ValueError:
                print("no more connection available")
                pass
        #print('disconnect success : ',addr);
        client_socket.close();
        #database.close();

def checkID(connection):
    global server_connection_list
    for con in server_connection_list:
        print('new : ',con['id'])
        if con['id'] == connection['id'] and con['type'] == connection['type']:
            try:
                server_connection_list.remove(con)
                if con['socket'] != connection['socket']:
                    #print('disconnect duplicated things : ', con['socket'])
                    con['socket'].close()
            except ValueError:
                pass
    server_connection_list.append(connection)
    #print(server_connection_list)


def fireOccurred(BC):
    global server_connection_list
    killThread = False
    while(True):
        for t in detectionThread:
            if t['buildingCode'] == BC:
                if not t['Fire']:
                    killThread = True
                    detectionThread.remove(t)
                    break
        if killThread:
            break


        for con in server_connection_list:
            if con['buildingCode'] == BC and con['type'] == 'hw':
                try:
                    con['socket'].sendall('SERVER PIX \n'.encode())
                    print('send OK')
                except:
                    print('HW sendFail')
                
        time.sleep(60)
    print('end of fire occ')


server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM);
server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1);
server_socket.bind(('', 4000));
server_socket.listen();
try:
    while True:
        client_socket, addr = server_socket.accept();
        th = threading.Thread(target=binder, args = (client_socket,addr));
        th.start();
except:
    print("server");
finally:
    server_socket.close();


