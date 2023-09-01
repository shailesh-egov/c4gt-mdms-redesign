from genson import SchemaBuilder
from dotenv import load_dotenv
import json, sys, os, io


generatedSchemas = {}

def readFiles(mdmsPath, schemaPath):
    """
    Generates master data schemas used for validation by reading existig master data json files

    Args:
        mdmsPath (String): Directory where the master data is stored
        schemaPath (String): Directory where the schemas will be written
    """
    for root, dirs, files in os.walk(mdmsPath):
        for file in files:
            if file.endswith(".json"):
                filePath = os.path.join(root, file)
                print("generating schema for {}".format(filePath))
                mdmsData = getFileData(filePath)
                masterName = getMasterName(mdmsData)

                mdmsData = mdmsData[masterName]

                if mdmsData != None:
                    try:
                        createJsonSchema(mdmsData, schemaPath, masterName)
                        print("Schema generated ".format(filePath))
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

def createJsonSchema(data, schemaPath, masterName):
    """
    Builds schemas from given master data and writes them to the given directory

    Args:
        masterData (Object): Master data object
        schemaPath (String): Directory where the schema will be written
        masterName (String): Name of the master data object used to name the schema file for respective schema

    """
    try:
        if not generatedSchemas.get(masterName):
            builder = SchemaBuilder(schema_uri="http://json-schema.org/draft-07/schema")
            builder.add_object(data)
            schema = builder.to_schema()
            fileName = "{}/{}.json".format(schemaPath, masterName)

            schema["title"] = masterName + " master data"

            fileName = fileName.lower().replace(".json", ".schema.json")
            print(fileName)
            try:
                with io.open(fileName, "w", encoding="utf-8") as f:
                    f.write(json.dumps(schema, indent=4, ensure_ascii=False))
                    f.close()
                    generatedSchemas[masterName] = 1
            except Exception as ex:
                print("Exception on writing schema on file.")
                raise ex
    except Exception as ex:
        raise ex


if __name__ == "__main__":

    """
    Reading env variables
    """
    load_dotenv()
    path = None
    mdmsPath = os.getenv("MDMS_DATA_PATH")
    mdmsSuffix = os.getenv("SCHEMA_SUFFIX")

    if mdmsPath is not None:
        schemaSuffix = mdmsSuffix
    if len(sys.argv) > 1:
        path = sys.argv[1]
    elif mdmsPath != None:
        path = mdmsPath
    else:
        print("Please provide mdms path")
        sys.exit()

    schemaPath = os.getenv("SCHEMA_PATH")

    schemaPathExist = os.path.exists(schemaPath)
    if not schemaPathExist:
        print("Please provide schema output path")
        sys.exit()


    """
    Build schema functionality
    """
    for path in os.listdir(mdmsPath):
        path = mdmsPath + "/" + path
        readFiles(path, schemaPath)
