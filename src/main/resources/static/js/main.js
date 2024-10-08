'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

var xhr = function(type, url, content) {
    return new Promise(function(resolve, reject) {
        var xmhr = new XMLHttpRequest();
        xmhr.open(type, url, true);
        xmhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        xmhr.send(JSON.stringify(content));
    });
};

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if(username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/websocket');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}


function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/'+username, onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/chat.register",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    )

    connectingElement.classList.add('hidden');
}


function onError(error) {
    connectingElement.textContent = 'Não foi possível se conectar ao WebSocket! Atualize a página e tente novamente ou entre em contato com o administrador.';
    connectingElement.style.color = 'red';
}

function send(event) {
    var messageType = document.querySelector('input[name="radio"]:checked').value;
    console.log(messageType)
    if(messageType == 'api'){
        sendStream(event);
    }else if(messageType == 'ws'){
        sendWs(event);
    }
    event.preventDefault();
}

function sendStream(event) {
    var messageContent = messageInput.value.trim();
    var chatMessage = {
                messageId: self.crypto.randomUUID(),
                sender: username,
                content: messageInput.value,
                type: 'CHAT'
            };
    JSON.stringify(chatMessage)
    xhr('POST', 'http://localhost:8081/temp/stream', chatMessage)
        .then(function(success){
            console.log(success);
        });
    messageInput.value = '';
    //onMessageReceived(chatMessage)
}

function sendWs(event) {
    var messageContent = messageInput.value.trim();

    if(messageContent && stompClient) {
        var chatMessage = {
            messageId: self.crypto.randomUUID(),
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        stompClient.send("/app/chat.send/"+username, {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}


function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var textElement = document.getElementById(message.messageId);
    if(textElement){
        console.log("element find")
        var messageText = document.createTextNode(message.content);
        textElement.appendChild(messageText);
        return;
    } else {
        console.log("element NOT find")
        textElement = document.createElement('p');
        textElement.setAttribute("id", message.messageId);
    }

    var messageElement = document.createElement('li');

    if(message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }

    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', send, true)