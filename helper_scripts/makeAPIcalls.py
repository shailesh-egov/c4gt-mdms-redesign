import requests
import os
import json

url = "http://localhost:8031/mdms/v1/_create/schema"
headers = {"Content-Type": "application/json"}


def getJSONSchema(dirPath):
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
                # print("\n\nreponse text: ", response.text)
            except Exception as ex:
                print(ex)
                print("Error in API call")


if __name__ == "__main__":
    dirPath = "generatedSchemas"
    getJSONSchema(dirPath)
