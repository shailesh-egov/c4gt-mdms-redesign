from genson import SchemaBuilder
from dotenv import load_dotenv
import json, sys, os, io
import requests
import json
from dotenv import load_dotenv
import sys

generatedSchemas = {}
url = "http://localhost:8031/mdms/v1/_create/schema"
headers = {"Content-Type": "application/json"}

id = 0
failCount = 0


def readFiles(mdmsPath, defaultPayload):
    """
    Generates master data schemas used for validation by reading existig master data json files
    And populates the database with the same
    Args:
        mdmsPath (String): Directory where the master data is stored
        defaultPayload (Dict): Common Payload
    """
    for tenantId in os.listdir(mdmsPath):
        modulesPath = mdmsPath + "/" + tenantId
        for moduleName in os.listdir(modulesPath):
            masterPath = modulesPath + "/" + moduleName
            for root, dirs, files in os.walk(masterPath):
                for file in files:
                    if file.endswith(".json"):
                        filePath = os.path.join(root, file)

                        mdmsData = getFileData(filePath)
                        masterName = getMasterName(mdmsData)

                        mdmsData = mdmsData[masterName]

                        if mdmsData != None:
                            try:
                                createJsonSchema(
                                    mdmsData,
                                    masterName,
                                    defaultPayload,
                                )
                                # print("Schema generated ".format(filePath))
                            except Exception as ex:
                                print("Schema feneration failed !")
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


def createJsonSchema(data, masterName, defaultPayload):
    """
    Builds schemas from given master data and writes them to the database

    Args:
        masterData (Object): Master data object
        masterName (String): Name of the master data object used to name the schema file for respective schema
        defaultPayload: (Dict): Common Payload
    """
    global failCount
    global id
    global generatedSchemas

    try:
        if masterName not in generatedSchemas:
            builder = SchemaBuilder(schema_uri="http://json-schema.org/draft-07/schema")
            builder.add_object(data)
            schema = builder.to_schema()
            id += 1
            schemaPayload = {
                "masterName": masterName,
                "masterDataSchema": schema,
            }
            defaultPayload["MDMSSchema"] = schemaPayload

            payload = json.dumps(defaultPayload)

            response = requests.request("POST", url, headers=headers, data=payload)

            if response.status_code == 400:
                failCount += 1
                print(
                    "Failed for this ID:",
                    id,
                    " masterName: ",
                    masterName,
                )
            else:
                generatedSchemas[masterName] = 1
    except Exception as ex:
        raise ex


if __name__ == "__main__":
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
    """
    Reading env variables
    """
    load_dotenv()
    path = None
    mdmsPath = os.getenv("MDMS_DATA_PATH")

    if len(sys.argv) > 1:
        path = sys.argv[1]
    elif mdmsPath != None:
        path = mdmsPath
    else:
        print("Please provide mdms path")
        sys.exit()

    """
    Populate schemas functionality
    """
    readFiles(mdmsPath, defaultPayload)

    print("\n\nFinal count: ", id + 1, "  failcount: ", failCount)
