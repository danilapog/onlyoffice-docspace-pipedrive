import { PipedriveFields } from "@components/fields";
import { Entity, Field } from "@components/fields/Fields";
import { AppContext, AppErrorType } from "@context/AppContext";
import { DocSpace, TFrameConfig } from "@onlyoffice/docspace-react";
import { Command } from "@pipedrive/app-extensions-sdk";
import { getFields, getFieldsData } from "@services/fields";
import { getCurrentURL } from "@utils/url";
import React, { useContext, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import AutomationApiSDK from "@utils/automation-api-sdk";
import { FieldData } from "src/types/fields";

const DOCSPACE_FRAME_ID = "docspace-editor-frame";
const PIPEDRIVE_ENTITIES = ["deal", "person", "organization", "product"];
const PIPEDRIVE_DIALOG_PADDING = 32;
const PIPEDRIVE_DIALOG_HEADER = 53;

const EditorPage: React.FC = () => { 
  const [id, setId] = useState<string>();
  const [mode, setMode] = useState<string>();
  const [entities, setEntities] = useState<Array<Entity>>();
  const [fieldsValues, setFieldsValues] = useState<Array<FieldData>>();
  const [automationApiSDK, setAutomationApiSDK] = useState<AutomationApiSDK>();

  const { t } = useTranslation();
  const { sdk, user, settings, setAppError } = useContext(AppContext);

  useEffect(() => {
    const { parameters } = getCurrentURL();
    
    sdk.execute(
      Command.RESIZE,
      {
        // @ts-ignore
        width: `calc(100vw - ${PIPEDRIVE_DIALOG_PADDING * 2}px)`,
        // @ts-ignore
        height: `calc(100vh - ${PIPEDRIVE_DIALOG_HEADER}px)`
      }
    );

    if (parameters.get("mode")) {
      setId("774803");
      setMode(parameters.get("mode") || "editor");
    } else {
      const data = JSON.parse(parameters.get("data") || "");
      setId(data.id);
      setMode("editor");
    }

    if (user && parameters.get("mode") !== "viewer") {
      Promise.all(
        PIPEDRIVE_ENTITIES.map((entity) => {
          return getFields(sdk, entity);
        })
      ).then((responses) => {
        var entities: Array<Entity> = [];

        for(var i = 0; i < PIPEDRIVE_ENTITIES.length; i++) {
          entities.push({
            id: PIPEDRIVE_ENTITIES[i],
            name: t(`pipedrive.entity.${PIPEDRIVE_ENTITIES[i]}`),
            fields: responses[i].fields
          });
        }

        setEntities(entities);
      })
    } else {
      console.log("fields-values");
      const dealId = Number(parameters.get("selectedIds"))
      getFieldsData(sdk, dealId).then((values) => {
        setFieldsValues(values.fieldsData);
      });
    }
  }, []);

  const onAppReady = () => {
    console.log("onAppReady"); //ToDo: dont work in 2.6.3

    const automationApiSDK = new AutomationApiSDK({
      // @ts-ignore
      docEditor: window.frames[0].DocEditor.instances["portal_editor"],
      // @ts-ignore
	    asc: window.frames[0].Asc
    });

    setAutomationApiSDK(automationApiSDK);
  }

  const onFieldClick = (entity: Entity, field: Field) => {
    console.log(field.fieldType);
    // switch (field.fieldType) {
    //   case ("int"):
    //   case ("varchar"):
    //   case ("monetary"):
        const textFieldProperties = {
          key: `pipedrive_${entity.id}_${field.key}`,
          placeholder: `${entity.name} ${field.name}`,
          tip: `${entity.name} ${field.name}`,
          tag: `pipedrive_${entity.id}_${field.key}`, //Todo: use key
        };
    
        automationApiSDK?.insertTextField(textFieldProperties);
    //     break;
    //   case ("date"):
        
    // }

   
  }

  const fillForm = () => {
    if (!fieldsValues) {
      return;
    }

    automationApiSDK?.setFormsData(fieldsValues);
  }

  const onRequestPasswordHash = () => user?.docspaceAccount?.passwordHash || "";

  const onLoadComponentError = () => {
    setAppError(AppErrorType.DOCSPACE_UNREACHABLE);
  };

  const onUnsuccessLogin = () => {
    setAppError(AppErrorType.DOCSPACE_AUTHORIZATION);
    // setLoading(false);
  };

  return (
    <div className="w-full h-full">
      <div className="flex w-full h-full flex-row">
        <div className="w-full h-full">
          {id && user && settings?.url && (
            <DocSpace
              url={settings?.url}
              email={user?.docspaceAccount?.userName || "undefined"}
              config={{
                frameId: DOCSPACE_FRAME_ID,
                mode: mode,
                id: id,
                width: "100%",
                height: "100%",
                // events: {
                //   onAppReady
                // } as unknown
              } as TFrameConfig }
              onLoadComponentError={onLoadComponentError}
              onRequestPasswordHash={onRequestPasswordHash}
              onUnsuccessLogin={onUnsuccessLogin}
            />
          )}
        </div>
        <div className="w-80 min-w-64 flex-shrink-0 overflow-y-auto">
          {!automationApiSDK && (
            <button onClick={onAppReady}>Connect while onAppReady dont work</button>
          )}
          {automationApiSDK && entities && mode === "editor" && (
            <PipedriveFields 
              entities={entities}
              onFieldClick={onFieldClick}
            />
          )}
          {automationApiSDK && mode === "viewer" && (
            <button onClick={fillForm}>Fill</button>
          )}
        </div>
      </div>
    </div>
  );
}
export default EditorPage;
