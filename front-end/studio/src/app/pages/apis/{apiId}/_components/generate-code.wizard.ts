/**
 * @license
 * Copyright 2018 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, ElementRef, Input, QueryList, ViewChildren} from "@angular/core";
import {ModalDirective} from "ngx-bootstrap/modal";
import {DropDownOption, DropDownOptionValue as Value} from "../../../../components/common/drop-down.component";
import {NgForm} from "@angular/forms";
import {LinkedAccount} from "../../../../models/linked-account.model";
import {LinkedAccountsService} from "../../../../services/accounts.service";
import {CodeEditorMode} from "../../../../components/common/code-editor.component";
import { ApisService } from "../../../../services/apis.service";
import { CodegenProject } from "../../../../models/codegen-project.model";
import { ConfigService } from "../../../../services/config.service";


export interface GenerateCodeWizardModel {
    artifactId: string;
    apiPackage: string;
    modelPackage: any;
    destinationPath: string;
}

var PROJECT_TYPES: DropDownOption[] = [
    new Value("Simple JAX-RS", "jaxrs"),
    new Value("Quarkus JAX-RS", "quarkus"),
    new Value("Thorntail JAX-RS", "thorntail"),
    new Value("Vert.x", "vertx", true),
    new Value("Spring Boot", "springboot", true),
    new Value("Node.js", "nodejs", true)
];


@Component({
    selector: "generate-code-wizard",
    templateUrl: "generate-code.wizard.html",
    styleUrls: [ "generate-code.wizard.css" ]
})
export class GenerateCodeWizardComponent {

    @ViewChildren("generateCodeModal") generateCodeModal: QueryList<ModalDirective>;

    @Input() apiId: string;

    protected _isOpen: boolean = false;

    public model: GenerateCodeWizardModel;
    public loading: boolean = false;
    public generating: boolean;
    public generated: boolean;
    public error: any = null;
    public errorMessage: string = null;
    public uiUrl: string;

    /**
     * Constructor with injection!
     * @param linkedAcounts
     */
    constructor(private linkedAcounts: LinkedAccountsService, private apis: ApisService, private config: ConfigService) {
        if (this.config.uiUrl()) {
            this.uiUrl = this.config.uiUrl();
        }
    }

    /**
     * Called to open the wizard.
     */
    public open(): void {
        this.error = null;
        this.errorMessage = null;
        // sample data to test
        // this.model = {
        //     "artifactId": "my-api",
        //     "apiPackage": "com.fastcode.demo.api",
        //     "modelPackage": "com.fastcode.demo.model",
        //     "destinationPath": "D:\projects"
        // };
        this.model = {
            "artifactId": "",
            "apiPackage": "",
            "modelPackage": "",
            "destinationPath": ""
        };
        this._isOpen = true;
        this.generateCodeModal.changes.subscribe( thing => {
            console.log("Generating code", this.generateCodeModal)
            console.log("Generating code", thing)
            if (this.generateCodeModal.first) {
                this.generateCodeModal.first.show();

            }
        });
    }

    /**
     * Called to close the wizard.
     */
    public close(): void {
        this._isOpen = false;
    }

    /**
     * Called when the user clicks "cancel".
     */
    protected cancel(): void {
        this.generateCodeModal.first.hide();
    }

    /**
     * Returns true if the wizard is open.
     * 
     */
    public isOpen(): boolean {
        return this._isOpen;
    }

    showGenerateButton() {
        if (!this.error) {
            return true;
        }
        return false;
    }

    public isGenerateButtonEnabled(): boolean {
        if (this.model.apiPackage && this.model.artifactId && this.model.destinationPath && this.model.modelPackage) {
            return true;
        } 
        return false;
    }

    public showCloseButton(): boolean {
        return this.generating || this.generated || this.error;
    }

    public isCloseButtonEnabled(): boolean {
        return this.generated || this.error;
    }

    public downloadFilename(): string {

        // TODO handle the other project types (jax-rs, node.js, etc)
        return "download.zip";
    }

    public generate(): void {
        this.generating = true;
        this.generated = false;
        this.apis.generateCode(this.apiId, this.model).subscribe(response => {
            this.saveFile(response.body);
            this.cancel();
        }, (error => {
            this.error = error;
            this.errorMessage = "Error generating code for current design.";
        }));
    }

    private saveFile(blob: Blob): void {
        const a = document.createElement('a');
        const url = window.URL.createObjectURL(blob);

        a.href = url;
        a.download = 'downloaded_file.zip';

        document.body.appendChild(a);
        a.click();

        // Clean up
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    }
}
