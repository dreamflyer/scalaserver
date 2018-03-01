import scalajs.js

import js.annotation._
import js.JSConverters._

@js.native
@JSGlobalScope
object Globals extends js.Object {
	var process: js.Any = js.native
}

@js.native
class JSTextDocument extends js.Any {
	def getText(): String = js.native;
	def uri: String = js.native;
}

@js.native
class JSDocumentChange extends js.Any {
	def document: JSTextDocument = js.native;
}

@JSImport("vscode-languageserver", "TextDocuments")
class JSTextDocuments extends js.Object {
	def all(): js.Array[JSTextDocument] = js.native;
	
	def listen(connection: VSCConnection): js.Any = js.native;
	
	def onDidChangeContent(arg: js.Function1[JSDocumentChange, js.Any]): js.Any = js.native;
	
	def syncKind: js.Any = js.native;
}

@JSImport("vscode-languageserver", "IPCMessageReader")
class JSIPCMessageReader(arg: js.Any) extends js.Object {

}

@JSImport("vscode-languageserver", "IPCMessageWriter")
class JSIPCMessageWriter(arg: js.Any) extends js.Object {

}

@js.native
@JSImport("vscode-languageserver", JSImport.Namespace)
object VSCodeServer extends js.Any {
	def createConnection(reader: JSIPCMessageReader, writer: JSIPCMessageWriter): VSCConnection = js.native;
}

@js.native
class JSTextDocumentPositionParams extends js.Any {

}

@js.native
class VSCConnection extends js.Any {
	def onInitialize(arg: js.Function1[js.Any, InitializeResult]): js.Any = js.native;
	def sendDiagnostics(diagnostics: SendDiagnostics): String = js.native;
	def onDidChangeConfiguration(arg: js.Function0[js.Any]): js.Any = js.native;
	def onCompletion(arg: js.Function1[JSTextDocumentPositionParams, js.Array[CompletionItem]]): js.Any = js.native;
	def onCompletionResolve(arg: js.Function1[CompletionItem, CompletionItem]): js.Any = js.native;
	
	def listen(): js.Any = js.native;
}

@ScalaJSDefined
class InitializeResult extends js.Object {
	var capabilities: InitializeResultCapabilities = null;
}

@ScalaJSDefined
class InitializeResultCapabilities extends js.Object {
	var textDocumentSync: js.Any = null;
	
	var completionProvider: InitializeResultCapabilitiesCompletionProvider = null;
}

@ScalaJSDefined
class InitializeResultCapabilitiesCompletionProvider extends js.Object {
	var resolveProvider: Boolean = false;
}

@ScalaJSDefined
class SendDiagnostics extends js.Object {
	var diagnostics: js.Array[Diagnostic] = null;
	var uri: String = null;
}

@ScalaJSDefined
class CompletionItem extends js.Object {
	var label: String = null;
	var detail: String = null;
	var documentation: String = null;
	
	var kind: Int = 0;
	var data: Int = 0;
}

@ScalaJSDefined
class Diagnostic extends js.Object {
	var severety: Int = 0;
	var range: JSClientRange = null;
	var message: String = null;
	var source: String = null;
}

@ScalaJSDefined
class JSPosition extends js.Object {
	var line: Int = 0;
	var character: Int = 0;
}

@ScalaJSDefined
class JSClientRange extends js.Object {
	var start: JSPosition = null;
	var end: JSPosition = null;
}

@ScalaJSDefined
class ServerConnection extends js.Object {
	var vscConnection: VSCConnection = VSCodeServer.createConnection(new JSIPCMessageReader(Globals.process), new JSIPCMessageWriter(Globals.process));
	
	var documents: JSTextDocuments = new JSTextDocuments();
	
	def start() {
		println("configure connection");
		
		this.documents.listen(this.vscConnection);
		
		this.vscConnection.onInitialize((params) => {
			var result = new InitializeResult();
			
			var capabilities = new InitializeResultCapabilities();
			
			var completionProvider = new InitializeResultCapabilitiesCompletionProvider();
			
			completionProvider.resolveProvider = true;
			
			capabilities.textDocumentSync = this.documents.syncKind;
			capabilities.completionProvider = completionProvider;
			
			result.capabilities = capabilities;
			
			result;
		});
		
		this.documents.onDidChangeContent((change: JSDocumentChange) => {
			this.validateTextDocument(change.document);
		});
		
		this.vscConnection.onDidChangeConfiguration(() => {
			this.documents.all().foreach((document: JSTextDocument) => {
				this.validateTextDocument(document);
			})
		});
		
		this.vscConnection.onCompletion((textDocumentPosition: JSTextDocumentPositionParams) => {
			println("completion request accepted");
			
			var result: js.Array[CompletionItem] = new js.Array();
			
			var item = new CompletionItem();
			
			item.label = "ScalaJavaScript";
			item.kind = 1;
			item.data = 1;
			
			result.push(item);
			
			result;
		});
		
		this.vscConnection.onCompletionResolve((item: CompletionItem) => {
			if (item.data == 1) {
				item.detail = "TypeScript details SCALA! SCALA!";
				item.documentation = "TypeScript documentation SCALA! SCALA!";
			} else if (item.data == 2) {
				item.detail = "JavaScript details SCALA!";
				item.documentation = "JavaScript documentation SCALA! SCALA!";
			}
			
			item;
		});
		
		this.vscConnection.listen();
	}
	
	def validateTextDocument(textDocument: JSTextDocument) {
		var diagnostics: js.Array[Diagnostic] = new js.Array();
		
		var lines = textDocument.getText().split("\n");
		
		var problems: Int = 0;
		
		var i: Int = 0;
		
		for(line <- lines) {
			var index: Int = line.indexOf("typescript");
			
			if(index >= 0) {
				problems = problems + 1;
				
				var range = new JSClientRange();
				
				var start = new JSPosition();
				var end = new JSPosition();
				
				start.line = i;
				start.character = index;
				
				end.line = i;
				end.character = index + 10;
				
				range.start = start;
				range.end = end;
				
				var diagnostic = new Diagnostic();
				
				diagnostic.severety = 2;
				diagnostic.range = range;
				diagnostic.message = "typescript should be spelled TypeScript SCALA SCALA SCALA1";
				diagnostic.source = "";
				
				diagnostics.push(diagnostic)
			}
			
			i = i + 1;
		}
		
		var sendDiagnostics = new SendDiagnostics();
		
		sendDiagnostics.uri = textDocument.uri;
		sendDiagnostics.diagnostics = diagnostics;
		
		this.vscConnection.sendDiagnostics(sendDiagnostics);
	}
}
