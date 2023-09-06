import os
from dotenv import load_dotenv
import sys
import json
import requests
import json

url = "http://localhost:8031/mdms/v1/_create"

id = 0
failCount = 0


def readFiles(mdmsPath, defaultPayload):
    """
    Reads master data  from the given directory and populates relational database with the same

    Args:
        mdmsPath (String): A path where master data files are present in JSON format
        payload (Object): A predefined payload object with things other than master data.
    """
    global id
    global failCount
    for tenantId in os.listdir(mdmsPath):
        modulesPath = mdmsPath + "/" + tenantId

        for moduleName in os.listdir(modulesPath):
            masterPath = modulesPath + "/" + moduleName
            for root, dirs, files in os.walk(masterPath):
                for file in files:
                    if file.endswith(".json"):
                        filePath = os.path.join(root, file)

                        id += 1
                        mdmsData = getFileData(filePath)
                        masterName = getMasterName(mdmsData)

                        mdmsData = mdmsData[masterName]

                        if mdmsData != None:
                            try:
                                defaultPayload["MDMSData"] = {
                                    "tenantId": tenantId,
                                    "moduleName": moduleName,
                                    "masterName": masterName,
                                    "masterData": mdmsData,
                                }
                                payload = json.dumps(defaultPayload)
                                headers = {"Content-Type": "application/json"}

                                response = requests.request(
                                    "POST", url, headers=headers, data=payload
                                )

                                if response.status_code == 400:
                                    failCount += 1
                                    print(
                                        "Failed for this ID:",
                                        id,
                                        " masterName: ",
                                        masterName,
                                        " in module : ",
                                        moduleName,
                                    )
                            except Exception as ex:
                                print("MDMS request failed")
                                print(ex)


def getFileData(filePath):
    """
    Returns master data object from a given file path

    Args:
        filePath (String): A path where master data file is present in JSON format

    Returns:
        masterData (Object): Master data object from the JSON file.
    """
    try:
        f = open(filePath, encoding="utf-8")
        data = json.load(f)

        if "tenantId" in data and "moduleName" in data:
            return data
        else:
            print("tenantId or moduleName is missing in the file: " + filePath)
            return None
    except Exception as ex:
        print(ex)
        print("JSON error in file - " + filePath)


def getMasterName(data):
    """
    Returns master name from the give master data

    Args:
        masterData (Object): Master data object

    Returns:
        masterName (String): Master name
    """
    masterName = None
    for key in data.keys():
        if key != "tenantId" and key != "moduleName" and data[key] is not None:
            masterName = key
    return masterName


if __name__ == "__main__":
    load_dotenv()
    path = None
    mdmsPath = os.getenv("MDMS_PATH")

    if len(sys.argv) > 1:
        path = sys.argv[1]
    elif mdmsPath != None:
        path = mdmsPath
    else:
        print("Please provide mdms path")
        sys.exit()

    defaultPayload = {
        "RequestInfo": {
            "apiId": "Rainmaker",
            "ver": None,
            "ts": None,
            "action": None,
            "did": None,
            "key": None,
            "msgId": "1686748436982|en_IN",
            "authToken": "a1352d56-07c7-4fd9-b4ff-b69781554df2",
            "correlationId": None,
            "plainAccessRequest": {"recordId": None, "plainRequestFields": []},
            "userInfo": {
                "id": 517,
                "userName": "NA1",
                "name": "NA",
                "type": "EMPLOYEE",
                "mobileNumber": "1234567890",
                "emailId": "",
                "roles": [
                    {
                        "id": None,
                        "name": "SUPER USER",
                        "code": "SUPERUSER",
                        "tenantId": None,
                    }
                ],
                "tenantId": "pg.citya",
                "uuid": "ba722111-0fbd-4a39-a4f0-0039f96eb69c",
            },
        },
    }

    # Read and populate functionality
    readFiles(path, defaultPayload)

    print("\n\nFinal count: ", id, "  failcount: ", failCount)
