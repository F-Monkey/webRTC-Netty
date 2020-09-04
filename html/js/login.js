var body = document.getElementById("body");
let loginForm;

addLoginInteractAria();

function addLoginInteractAria(){
    loginForm = document.createElement("div");
    let userIdSpan = document.createElement("span");
    userIdSpan.innerHTML = "username:";
    let userIdInput = document.createElement("input");
    userIdInput.type = "text";
    let passwordSpan = document.createElement("span");
    passwordSpan.innerHTML = "password:";
    let passwordInput = document.createElement("input");
    passwordInput.type = "text";
    let loginBtn = document.createElement("input");
    loginBtn.type = "button";
    loginBtn.value = "login";

    loginForm.appendChild(userIdSpan);
    loginForm.appendChild(userIdInput);
    loginForm.appendChild(document.createElement("br"));
    loginForm.appendChild(passwordSpan);
    loginForm.appendChild(passwordInput);
    loginForm.appendChild(loginBtn);
    body.appendChild(loginForm);
    loginBtn.addEventListener("click",function(){
        let userId = userIdInput.value;
        if(userId == 'undefined' || userId == ""){
            alert("请输入用户id");
            return;
        }
        let password = passwordInput.value;
        let login = UserCmd_Login.create({userId:userId,password:password});
        let content = UserCmd_Login.encode(login).finish();
        // TODO
        let hiddenUserIdElement = document.createElement("input");
        hiddenUserIdElement.type = "hidden";
        hiddenUserIdElement.value = userId;
        hiddenUserIdElement.id = "userId";
        document.body.appendChild(hiddenUserIdElement);

        sendMsg(createCmdBuffer(LOGIN,content));
        cmd_list.push(LOGIN);
    });
}


message_callback_dict[LOGIN] = function(content) {
    let loginResult = UserCmd_LoginResult.decode(content);
    let roomScript = document.createElement("script");
    roomScript.src = "js/room.js";
    document.body.appendChild(roomScript);
    loginForm.remove(loginForm.selectedIndex);
    console.info("login success");
}

