let message_callback_dict = {};
let content_dict = {};
let cmd_list = [];

function removeCmdFromCmdList(cmdType){
    for( var i in cmd_list){
        if(cmdType == cmd_list[i]){
            cmd_list.slice(i);
            return true;
        }
    }
    return false;
}

var ws = new WebSocket("ws://localhost:8080/ws");
ws.onopen = function(){
   console.log("open");
}

ws.onmessage = function(e){
    let reader = new FileReader();
    reader.readAsArrayBuffer(e.data);
    reader.onload= (e) => {
        var buf = new Uint8Array(reader.result);
        let packageGroup = PackageGroup.decode(buf);
        let packages = packageGroup.packages;
        for (let i in packages) {
            let package = packages[i];
            let resultMsg = package.resultMsg;

            if(resultMsg.code != 200){
                alert(resultMsg.msg)
                continue;
            }

            let func = message_callback_dict[cmdType];
            if(func){
                func(package.content);
            }

        }
    };
}
ws.onclose = function(e){
　　console.log("close");
}

ws.onerror = function(error){
　　console.log(error);
}

function sendMsg(buffer){
    console.info("buffer: " + buffer);
    ws.send(buffer);
}