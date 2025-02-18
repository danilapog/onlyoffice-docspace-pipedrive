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

import React, { useEffect, useState } from "react";

export type Field = {
  id: number,
  key: string,
  name: string,
  fieldType: string;
}

export type Entity = {
  id: string,
  name: string,
  fields: Array<Field>
}

export type FieldsProps = {
  entities: Array<Entity>
  onFieldClick: (entity: Entity, field: Field) => void
};

export const PipedriveFields: React.FC<FieldsProps> = ({
  entities,
  onFieldClick
}) => {
  const [search, setSearch] = useState<string>("");

  return(
    <div className="px-4">
      <div>
        <h3>Pipedrive fields</h3>
      </div>

      <div className="py-2">
        <input
         value={search}
         onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <div className="py-2">
        {entities.map((entity) => {
          return(
            <FieldsBlock
              key={entity.id}
              entity={entity}
              search={search}
              onFieldClick={onFieldClick}
            />
          );
        })}
      </div>
    </div>
  );
};

type FieldsBlockProps = {
  entity: Entity,
  search?: string,
  onFieldClick: (entity: Entity, field: Field) => void
};

const FieldsBlock: React.FC<FieldsBlockProps> = ({
  entity,
  search = "",
  onFieldClick
}) => {
  const [expand, setExpand] = useState<boolean>(false);

  useEffect(() => {
    setExpand(search.length > 0);
  }, [search]);

  return(
    <div>
      <button onClick={() => setExpand(!expand)}>{entity.name}</button>
      <div hidden={!expand}>
        <ul>
          <div className="px-2">
            {entity.fields.map((field) => {
              return (
                <li
                  key={field.key}
                  hidden={search.length > 0 && !field.name.toLowerCase().includes(search.toLowerCase())}
                >
                  <button
                    onClick={() => {onFieldClick(entity, field)}}
                  >
                    {entity.name} {field.name}
                  </button>
                </li>
              );
            })}
          </div>
        </ul>
      </div>
    </div>
  );
};
