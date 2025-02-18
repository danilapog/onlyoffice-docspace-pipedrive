/**
 *
 * (c) Copyright Ascensio System SIA 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {
  Options,
  DocEditor,
  Connector,
  Asc,
  TextField,
  FormData
} from "./types";

class AutomationApiSDK {
  private docEditor: DocEditor;
  private asc: Asc;
  private connector: Connector;

  constructor(options: Options) {
    const { docEditor, asc } = options;

    this.docEditor = docEditor;
    this.asc = asc; 

    this.connector = this.docEditor.createConnector();
	}

  public insertTextField(properties: TextField) {
    this.setAscScopeData(properties);

    this.connector.callCommand(() => {
      // @ts-ignore
      var oDocument = Api.GetDocument();
      // @ts-ignore
      var oTextForm = Api.CreateTextForm(Asc.scope.data);
      // @ts-ignore
      var oParagraph = Api.CreateParagraph();
      oParagraph.AddElement(oTextForm);
      oDocument.InsertContent([oParagraph], true, { KeepTextOnly: true });
    });

    this.docEditor.grabFocus();
  }

  public setFormsData(data: Array<FormData>) {
    this.setAscScopeData(data);

    this.connector.callCommand(() => {
      // @ts-ignore
      var oDocument = Api.GetDocument();
      // @ts-ignore
      oDocument.SetFormsData(Asc.scope.data);
    });
  }

  private setAscScopeData(data: Object) {
    this.asc.scope.data = data;
  }

}
export default AutomationApiSDK;