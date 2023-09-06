import json
import requests

url = "http://localhost:8031/mdms/v1/_create/config"
headers = {"Content-Type": "application/json"}

id = 0
failCount = 0


def populateDBWithMasterConfigData(masterConfigMap, defaultPayload):
    """
    Reads master config data  used for fallback functionality from the master-config.json
    And populates into a relational DB with following schema
    Schema : (id: UUID, is_state_level: boolean,master_name: String,module_name: String, unique_keys: String[])

    Args:
        masterConfigMap (Object): master config object from master-config.json file
    """
    global id
    global failCount
    for moduleName in masterConfigMap.keys():
        configPayload = {}
        configPayload["moduleName"] = moduleName
        module = masterConfigMap[moduleName]

        for masterName in module.keys():
            id += 1
            configData = module[masterName]

            for key, value in configData.items():
                configPayload[key] = value

            defaultPayload["MDMSConfig"] = configPayload
            payload = json.dumps(defaultPayload)

            response = requests.request("POST", url, headers=headers, data=payload)

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

    filePath = "master-config.json"
    f = open(filePath, encoding="utf-8")
    masterConfigMap = json.load(f)

    populateDBWithMasterConfigData(masterConfigMap, defaultPayload)
    print("\n\nFinal count: ", id, "  failcount: ", failCount)
