import requests
import os
import json
from dotenv import load_dotenv
import sys

url = "http://localhost:8031/mdms/v1/_create/schema"
headers = {"Content-Type": "application/json"}

count = 0
failCount = 0


def populateMasterSchemaDB(dirPath, defaultPayload):
    """
    Reads master data schemas from the given directory and populates the DB with them.

    Args:
        dirPath (String): Directory where the schemas are stored.
    """
    global count
    global failCount
    for root, dirs, files in os.walk(dirPath):
        for file in files:
            masterName = file.split(".")[0]
            filePath = dirPath + "/" + file

            try:
                f = open(filePath, encoding="utf-8")
                schema = json.load(f)
            except Exception as ex:
                print(ex)
                print("JSON error in file - " + filePath)

            schemaPayload = {"masterName": masterName, "masterDataSchema": schema}
            defaultPayload["MDMSSchema"] = schemaPayload

            payload = json.dumps(defaultPayload)

            response = requests.request("POST", url, headers=headers, data=payload)

            if response.status_code == 400:
                failCount += 1
                print("Failed for this ID:", id, " masterName: ", masterName)
            count += 1


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

    dirPath = "generatedSchemas"
    populateMasterSchemaDB(dirPath, defaultPayload)

    print("\n\nFinal count: ", count, "  failcount: ", failCount)
