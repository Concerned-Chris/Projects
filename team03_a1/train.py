import csv
import sys

def readProblemFile(name):
    '''
    number = row[0]
    startStation = row[1]
    destinationStation = row[2]
    schedule = row[3]
    costFunction = row[4]
    '''

    with open(name, 'r') as file:
        csvreader = csv.reader(file)
        header = next(csvreader)
        problems = []
        for row in csvreader:
            problem = Problem(row[0], row[1], row[2], row[3], row[4])
            problems.append(problem)
    file.close()
    return problems

def readSchedule(schedule):
    '''
    Train No. = row[0]
    train Name = row[1]
    islno = row[2]
    station Code = row[3]
    Station Name = row[4]
    Arrival time = row[5]
    Departure time = row[6]
    Distance = row[7]
    Source Station Code = row[8]
    source Station Name = row[9]
    Destination station Code = row[10]
    Destination Station Name = row[11]
    '''

    with open(schedule, 'r') as file:
        csvreader = csv.reader(file)
        header = next(csvreader)
        prev = ""
        prevDistance = 0
        stations = []
        prevStationList = []
        prevDepartureTime = ""
        for row in csvreader:
            islno = int(row[2])
            stationCode = row[3].strip()
            if (islno == 1):
                prevStationList = []
                station = Station(stationCode,  islno, schedule)
                #https://stackoverflow.com/questions/7125467/find-object-in-list-that-has-attribute-equal-to-some-value-that-meets-any-condi
                testStation = next((x for x in stations if x.stationCode == station.stationCode), None)
                if testStation == None:
                    stations.append(station)
                prev = stationCode
                prevDistance = int(row[7])
                prevStationList.append(station)
                prevDepartureTime = row[6].replace("'","")
            else:
                station = Station(stationCode,  islno, schedule)
                # if station not in stations add it
                testStation = next((x for x in stations if x.stationCode == station.stationCode), None)
                if testStation == None:
                    stations.append(station)
                prevStation = next((x for x in stations if x.stationCode == prev), None)
                if not(prevStation == None):
                    arrivalTime = row[5].replace("'", "")
                    timeDistance = subTimes(arrivalTime, prevDepartureTime)
                    distance = int(row[7]) - prevDistance
                    connection = Connection(prev, islno - 1, stationCode, islno, row[0].replace("'", ""), distance, 1, False)
                    #distance and time distance can be different!!!
                    #only track shortest distance and update the trainNr
                    #for timeDistance track multiple because depature times
                    testConnection = next((x for x in prevStation.connectionList if x.stationCodeFrom == prev and x.stationCodeTo == stationCode and not x.fake), None)
                    if testConnection is None:
                        prevStation.connectionList.append(connection)
                        connection.departureTimesAndTimeDistance.append((timeDistance, prevDepartureTime))
                        connection.trainNrs.append(row[0].replace("'", ""))
                        connection.islNos.append((islno - 1, islno))
                    else:
                        #TODO: Test if this works !!!
                        if distance < testConnection.distance:
                            testConnection.distance = distance
                            testConnection.trainNr = row[0].replace("'", "")
                            testConnection.islNoFrom = islno - 1
                            testConnection.islNoTo = islno
                        testConnection.departureTimesAndTimeDistance.append((timeDistance, prevDepartureTime))
                        testConnection.trainNrs.append(row[0].replace("'", ""))
                        testConnection.islNos.append((islno - 1, islno))
                    if (islno > 10):
                        shortcutList = prevStationList[0:(islno - 10)].copy()
                        for i in range(0, len(shortcutList)):
                            con = Connection(shortcutList[i].stationCode, i+1, row[3].strip(), islno, row[0].replace("'", ""), 9999999999999999999999, 10, True)
                            con.departureTimesAndTimeDistance.append(("99999999999999999999:23:59:59", "00:00:00"))
                            testConnection = next((x for x in shortcutList[i].connectionList if x.stationCodeFrom == shortcutList[i].stationCode and x.stationCodeTo == stationCode), None)
                            if testConnection == None:
                                # !!!  objects in shortcutList are not the same as in stations !!!!
                                y = next((x for x in stations if shortcutList[i].stationCode == x.stationCode))
                                y.connectionList.append(con)
                prev = stationCode
                prevDistance = int(row[7])
                prevStationList.append(station)
                prevDepartureTime = row[6].replace("'", "")

    file.close()
    return stations

#https://www.pythontutorial.net/python-basics/python-write-csv-file/
def writeSolution(data):
    header = ['ProblemNo', 'Connection', 'Cost']
    with open('solution.csv', 'w', encoding='UTF8', newline='') as file:
        writer = csv.writer(file)

        writer.writerow(header)

        writer.writerows(data)
    return

def dijkstra(stations, startStation, destinationStation, costFunction):
    #https://de.wikipedia.org/wiki/Dijkstra-Algorithmus
    #https://www.algorithms-and-technologies.com/de/dijkstra/Python
    startStation.stops = 0
    startStation.distance = 0
    startStation.price = 0
    if costFunction.startswith("a"):
        tmp = costFunction.split()
        costFunction = tmp[0]
        startStation.currentTime = tmp[1]
        startStation.travelTime = tmp[1]
    stationsCopy = stations.copy()
    while not(len(stationsCopy) == 0):
        u = getStationsWithMinCost(stationsCopy, costFunction)
        if u is None:
            break
        if u in stationsCopy:
            stationsCopy.remove(u)
        #stopping wenn the destinationStation is entered makes the algorithm faster, but could lead to suboptimal solutions
        if(u.stationCode == destinationStation.stationCode):
            return destinationStation.pred
        for con in u.connectionList:
            #if the costFunction is not price the shortcut/fake connections may not be taken !!!
            if not(costFunction == "price"):
                if con.fake:
                    continue
            v = next((x for x in stations if x.stationCode == con.stationCodeTo and con.stationCodeFrom == u.stationCode), None)
            #if u.stationCode == "PUNE" and v.stationCode == "BRC":
                #print("before")
                #print(u.price + con.price)
                #print(v.price)
            if v.stationCode == u.stationCode:
                continue
            if v in stationsCopy:
                costUpdate(u, v, costFunction, con)
            #if v.stationCode == "BRC":
                #print("after")
                #print(v.price)
    return destinationStation.pred

def getStationsWithMinCost(stations, costFunction):
    min = 1000000000
    minStation = None
    for station in stations:
        if costFunction == "stops":
            if station.stops < min:
                min = station.stops
                minStation = station
        if costFunction == "distance":
            if station.distance < min:
                min = station.distance
                minStation = station
        if costFunction == "price":
            if station.price < min:
                min = station.price
                minStation = station
        if costFunction == "arrivaltime":
            x = timeToInt(station.travelTime)
            if x < min:
                min = timeToInt(station.travelTime)
                minStation = station
    return minStation

def costUpdate(u, v, costFunction, con):
    if costFunction == "stops":
        if con.fake:
            return
        alt = u.stops + 1
        if alt < v.stops:
            v.stops = alt
            v.pred = u
            v.saveCon = con
    if costFunction == "distance":
        #conUV = next((x for x in u.connectionList if x.stationCodeFrom == u.stationCode and x.stationCodeTo == v.stationCode),None)
        #if conUV is not None:
        if con.fake:
            return
        alt = u.distance + con.distance
        if alt < v.distance:
            v.distance = alt
            v.pred = u
            v.saveCon = con
    if costFunction == "price":
        #conUV = next((x for x in u.connectionList if x.stationCodeFrom == u.stationCode and x.stationCodeTo == v.stationCode),None)
        #if conUV is not None:
        alt = u.price + con.price
        if alt < v.price:
            v.price = alt
            v.pred = u
            v.saveCon = con
    if costFunction == "arrivaltime":
        #conUV = next((x for x in u.connectionList if x.stationCodeFrom == u.stationCode and x.stationCodeTo == v.stationCode and not x.fake),None)
        #if conUV is not None:
        if con.fake:
            return
        tup = shortestTravelTime(u, v, con)
        shortestTraveltimeUV = tup[0]
        if shortestTraveltimeUV < v.travelTime:
            v.travelTime = shortestTraveltimeUV
            timeArray = v.travelTime.split(":")
            if len(timeArray) > 3:
                v.currentTime = timeArray[1] + ":" + timeArray[2] + ":" + timeArray[3]
            else:
                v.currentTime = v.travelTime
            v.pred = u
            v.trainNr = con.trainNrs[tup[1]]
            con.trainNr = con.trainNrs[tup[1]]
            con.islNoFrom = con.islNos[tup[1]][0]
            con.islNoTo = con.islNos[tup[1]][1]
            v.saveCon = con
    return

def shortestTravelTime(u, v, conUV):
    index = 0
    flag = False
    currentTime = u.currentTime
    minTime = "999999999999:23:59:59"
    for tupleTime in conUV.departureTimesAndTimeDistance:
        if not (u.trainNr == conUV.trainNrs[conUV.departureTimesAndTimeDistance.index(tupleTime)] or u.trainNr == -1):
            currentTime = addTimes(currentTime, "00:05:00", False)
            flag = True
        sub = timeDistance(currentTime, tupleTime[1])
        sumTime = addTimes(u.travelTime, sub, True)
        sumTime = addTimes(sumTime, tupleTime[0], True)
        if flag:
            sumTime = addTimes(sumTime, "00:05:00", True)
        if timeToInt(sumTime) < timeToInt(minTime):
            minTime = sumTime
            index = conUV.departureTimesAndTimeDistance.index(tupleTime)
        currentTime = u.currentTime
        flag = False
    return minTime, index

def timeDistance(time1, time2):
    if timeToInt(time1) < timeToInt(time2):
        return intToTime(timeToInt(time2) - timeToInt(time1))
    else:
        return intToTime((24 * 60 * 60) - (timeToInt(time1) - timeToInt(time2)))

def usedConnections(destinationStation, costFunktion):
    usedConnections = []
    path = ""
    u = destinationStation
    while u.pred is not None:
        part = u.pred.stationCode + " -> " + u.stationCode + " ,"
        path = part + path
        '''
        if costFunktion == "price":
            connection = next((x for x in u.pred.connectionList if x.stationCodeFrom == u.pred.stationCode and x.stationCodeTo == u.stationCode), None)
        else:
            connection = next((x for x in u.pred.connectionList if x.stationCodeFrom == u.pred.stationCode and x.stationCodeTo == u.stationCode and not x.fake), None)
        if connection is not None:
            usedConnections.insert(0, connection)
        '''
        usedConnections.insert(0, u.saveCon)
        u = u.pred
    return (path, usedConnections)

def usedConnectionsToConnection(usedConnections):
    connection = ""
    usedConnectionsCopy = usedConnections.copy()
    trainNr = 0
    islNoFrom = 0
    islNoTo = 0
    trainChange = True
    while (len(usedConnectionsCopy) > 1):
        if trainChange:
            trainNr = usedConnectionsCopy[0].trainNr
            islNoFrom = usedConnectionsCopy[0].islNoFrom
            trainChange = False
        if not(usedConnectionsCopy[0].trainNr == usedConnectionsCopy[1].trainNr):
            trainChange = True
            islNoTo = usedConnectionsCopy[0].islNoTo
            connection = connection + str(trainNr) + " : " + str(islNoFrom) + " -> " + str(islNoTo) + " ; "
        usedConnectionsCopy.pop(0)
    #if trainChange is False last connection is part of the train before loop finish
    if trainChange:
        trainNr = usedConnectionsCopy[0].trainNr
        islNoFrom = usedConnectionsCopy[0].islNoFrom
        islNoTo = usedConnectionsCopy[0].islNoTo
        connection = connection + str(trainNr) + " : " + str(islNoFrom) + " -> " + str(islNoTo)
    else:
        islNoTo = usedConnectionsCopy[0].islNoTo
        connection = connection + str(trainNr) + " : " + str(islNoFrom) + " -> " + str(islNoTo)
    return connection

def resetStations(stations):
    for station in stations:
        station.pred = None
        station.stops = 100000
        station.distance = 10000000
        station.price = 100000000
        station.currentTime = "00:00:00"
        station.travelTime = "99999999:23:59:59"

        station.saveCon = None
    return stations

def addTimes(time1, time2, flag):
    timeList1 = time1.split(":")
    timeList2 = time2.split(":")
    minutesCary = 0
    hoursCary = 0
    daysCary = 0
    if(len(timeList1) == 4):
        days1 = int(timeList1[0])
        hours1 = int(timeList1[1])
        minutes1 = int(timeList1[2])
        seconds1 = int(timeList1[3])
    else:
        days1 = 0
        hours1 = int(timeList1[0])
        minutes1 = int(timeList1[1])
        seconds1 = int(timeList1[2])
    if (len(timeList2) == 4):
        days2 = int(timeList2[0])
        hours2 = int(timeList2[1])
        minutes2 = int(timeList2[2])
        seconds2 = int(timeList2[3])
    else:
        days2 = 0
        hours2 = int(timeList2[0])
        minutes2 = int(timeList2[1])
        seconds2 = int(timeList2[2])
    seconds = seconds1 + seconds2
    if seconds > 59:
        seconds = seconds - 60
        minutesCary = 1
    minutes = minutes1 + minutes2 + minutesCary
    if minutes > 59:
        minutes = minutes - 60
        hoursCary = 1
    hours = hours1 + hours2 + hoursCary
    if hours > 23:
        hours = hours - 24
        daysCary = 1
    days = days1 + days2 + daysCary
    result = ""
    if days > 0 and days < 10 and flag:
        result = result + "0" + str(days) + ":"
    if days >= 10 and flag:
        result = result + str(days) + ":"
    result = result + intToString(hours) + ":" + intToString(minutes) + ":" + intToString(seconds)
    return result

def subTimes(time1, time2):
    timeList1 = time1.split(":")
    timeList2 = time2.split(":")
    hours1 = int(timeList1[0])
    minutes1 = int(timeList1[1])
    seconds1 = int(timeList1[2])
    hours2 = int(timeList2[0])
    minutes2 = int(timeList2[1])
    seconds2 = int(timeList2[2])
    time1Int = 60 * 60 * hours1 + 60 * minutes1 + seconds1
    time2Int = 60 * 60 * hours2 + 60 * minutes2 + seconds2
    if time1Int < time2Int:
        time1Int = time1Int + 24 * 60 * 60
    timeDistanceInt = time1Int - time2Int
    timeDistance = intToTime(timeDistanceInt)
    return timeDistance

def intToTime(intTime):
    hours = int(intTime/(60*60))
    intTime = intTime - hours * 60 * 60
    minutes = int(intTime/60)
    intTime = intTime - minutes * 60
    seconds = intTime
    return intToString(hours)+":"+intToString(minutes)+":"+intToString(seconds)

def intToString(i):
    if i < 10:
        return "0" + str(i)
    else:
        return str(i)

def closestTime(time1, timeList):
    closestTime = ""
    closestIntTime = 1000000
    intTime1 = timeToInt(time1)
    for time2 in timeList:
        intTime2 = timeToInt(time2)
        if intTime2 < intTime1:
            intTime2 = intTime2 + 24 * 60 * 60
        if intTime2 - intTime1 < closestIntTime:
            closestIntTime = intTime2 - intTime1
            closestTime = time2
    return closestTime

def timeToInt(time):
    splitTime = time.split(":")
    if len(splitTime) == 4:
        result = int(splitTime[0]) * 24 * 60 * 60 + int(splitTime[1]) * 60 * 60 + int(splitTime[2]) * 60 + int(splitTime[3])
    else:
        result = int(splitTime[0]) * 60 * 60 + int(splitTime[1]) * 60 + int(splitTime[2])
    return result

class Station:
    def __init__(self, stationCode, islNo, schedule):
        self.stationCode = stationCode
        self.schedule = schedule
        self.islno = islNo
        self.connectionList = []
        self.pred = None
        self.stops = 100000
        self.distance = 10000000
        self.price = 100000000
        self.currentTime = "00:00:00"
        self.travelTime = "99999999999:23:59:59"
        self.trainNr = -1

        self.saveCon = None

    def addStopToStopList(self, stop, train):
        self.toList.add((stop, train))

class Connection:
    def __init__(self, stationCodeFrom, islNoFrom, stationCodeTo, islNoTo, trainNr, distance, price, fake):
        self.stationCodeFrom = stationCodeFrom
        self.islNoFrom = islNoFrom
        self.stationCodeTo = stationCodeTo
        self.islNoTo = islNoTo
        self.trainNr = trainNr
        self.distance = distance
        self.price = price
        self.fake = fake
        self.departureTimesAndTimeDistance = []
        self.trainNrs = []
        self.islNos = []

class Problem:
    def __init__(self, number, startStation, destinationStation, schedule, costFunction):
        self.number = number
        self.startStation = startStation
        self.destinationStation = destinationStation
        self.schedule = schedule
        self.costFunction = costFunction

def main():
    #print(addTimes("1:20:50:50", "2:04:10:12"))
    #timeList = ["00:00:00", "10:00:00", "15:00:00", "20:00:00"]
    #print(closestTime("12:00:00", timeList))
    #print(subTimes("23:55:55", "22:15:59"))
    #print(subTimes("00:00:00", "22:00:00"))

    #print(timeDistance("01:00:00", "01:03:00"))
    #print(timeDistance("01:05:00", "01:03:00"))

    #sub = timeDistance("23:10:00", "19:20:00")
    #sumTime = addTimes("01:23:05:00", sub, True)
    #print(sumTime)
    #sumTime = addTimes(sumTime, "00:05:00", True)
    #print(sumTime)

    args = sys.argv[1:]
    filename = args[0]
    start = int(args[1])
    end = int(args[2])

    # edit the name in the next line to import different problems
    problems = readProblemFile(filename)
    # edit the next line to solve more problems
    data = []
    schedulePred = ""
    for i in range(start, end):
        print(problems[i].number)
        flagNotReadMini = True
        flagNotRead = True
        if (problems[i].schedule == "mini-schedule.csv"):
            if flagNotReadMini:
                stationsMini = readSchedule(problems[i].schedule)
                flagNotReadMini = False
            else:
                stationsMini = resetStations(stationsMini)
            stations = stationsMini.copy()
        else:
            if flagNotRead:
                stationsBig = readSchedule(problems[i].schedule)
                flagNotRead = False
            else:
                stationsBig = resetStations(stationsBig)
            stations = stationsBig.copy()

        #if (problems[i].schedule == schedulePred):
            #stations = resetStations(stations)
        #else:
            #stations = readSchedule(problems[i].schedule)

        costFunction = problems[i].costFunction
        # only for testing

        '''
        for station in stations:
            if station.stationCode == "PUNE":
                #print("PUNE")
                #print(stations.index(station))
                for con in station.connectionList:
                    if (con.stationCodeFrom == "PUNE" and con.stationCodeTo == "BRC"):
                        print(station.connectionList.index(con))
            #if station.stationCode == "PLJ":
                #print("PLJ")
                #print(stations.index(station))
        '''

        '''
        for j in range(0, 4309):
            if stations[j].stationCode == "AWY":
                print("AWY " + str(j))
            if stations[j].stationCode == "ERS":
                print("ERS " + str(j))
            if stations[j].stationCode == "IPL":
                print("IPL " + str(j))
        '''


        '''
        for j in range(0, 4309):
            if stations[j].stationCode == "ET":
                for z in range(0, len(stations[j].connectionList)):
                    if stations[j].connectionList[z].stationCodeTo == "NZM":
                        print(z)
        '''

        startStation = next((x for x in stations if x.stationCode == problems[i].startStation), None)
        destinationStation = next((x for x in stations if x.stationCode == problems[i].destinationStation), None)

        #print(startStation.stationCode)
        #print(destinationStation.stationCode)
        test = dijkstra(stations, startStation, destinationStation, costFunction)
        # print(destinationStation.stops)
        (path, usedCons) = usedConnections(destinationStation, costFunction)
        # print(path)
        con = usedConnectionsToConnection(usedCons)
        # print(con)
        if (problems[i].costFunction == "stops"):
            dataRow = [i, con, destinationStation.stops]
        if (problems[i].costFunction == "distance"):
            dataRow = [i, con, destinationStation.distance]
        if (problems[i].costFunction == "price"):
            dataRow = [i, con, destinationStation.price]
        if (problems[i].costFunction.startswith("a")):
            #print(destinationStation.currentTime)
            #print(destinationStation.travelTime)
            dataRow = [i, con, destinationStation.travelTime]
        data.append(dataRow)
        schedulePred = problems[i].schedule
    writeSolution(data)

if __name__ == "__main__":
    main()