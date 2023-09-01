import requests
import os
import json
from dotenv import load_dotenv
import sys

url = "http://localhost:8031/mdms/v1/_create/schema"
headers = {"Content-Type": "application/json"}


def getJSONSchema(dirPath):
    """
    Reads master data schemas from the given directory and populates the DB with them.

    Args:
        dirPath (String): Directory where the schemas are stored.
    """
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

            try:
                payload = json.dumps(
                    {"masterName": masterName, "masterDataSchema": schema}
                )
                response = requests.request("POST", url, headers=headers, data=payload)
                print(response.status_code)
            except Exception as ex:
                print(ex)
                print("Error in API call")


if __name__ == "__main__":
    """
    Reading env variables
    """
    load_dotenv()
    schemaPath = os.getenv("SCHEMA_PATH")

    schemaPathExist = os.path.exists(schemaPath)
    if not schemaPathExist:
        print("Please provide schema output path")
        sys.exit()
    
    getJSONSchema(schemaPath)
