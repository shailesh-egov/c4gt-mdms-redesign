import json
import requests

url = "http://localhost:8031/mdms/v1/_create/config"
headers = {"Content-Type": "application/json"}


def getDataFromMasterConfig(masterConfigMap):
    """
    Reads master config data  used for fallback functionality from the master-config.json
    And populates into a relational DB with following schema
    Schema : (id: UUID, is_state_level: boolean,master_name: String,module_name: String, unique_keys: String[])
    
    Args:
        masterConfigMap (Object): master config object from master-config.json file
    """
    for moduleName in masterConfigMap.keys():
        payload = {}
        payload["moduleName"] = moduleName
        module = masterConfigMap[moduleName]

        for masterName in module.keys():
            configData = module[masterName]

            for key, value in configData.items():
                payload[key] = value
        payload = json.dumps(payload)
        response = requests.request("POST", url, headers=headers, data=payload)
        print(response.status_code)


if __name__ == "__main__":
    filePath = "master-config.json"
    f = open(filePath, encoding="utf-8")
    masterConfigMap = json.load(f)

    getDataFromMasterConfig(masterConfigMap)
