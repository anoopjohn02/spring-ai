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

var host = "http://localhost:8081";

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
    stompClient.subscribe('/topic/'+username, onWsMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/chat.register",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    )

    connectingElement.classList.add('hidden');
}


function onError(error) {
    connectingElement.textContent = 'Unable to connect to WebSocket! Use Stream API.';
    connectingElement.style.color = 'red';
}

function send(event) {
    var messageType = document.querySelector('input[name="radio"]:checked').value;
    //console.log(messageType)
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
    var msg = JSON.stringify(chatMessage)
    const streamUrl = host + '/v1/stream/temp';
    fetch(streamUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: msg
    }).then(response => {
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        //console.log("got response")
        function readStream() {
            //console.log("reading chunk");
            return reader.read().then(({ done, value }) => {
                if (done) {
                    console.log('Stream finished.');
                    return;
                }
                // Decode and display each chunk
                const chunk = decoder.decode(value, { stream: true });
                //console.log("Received = "+chunk);
                const payload = chunk.slice(5).trim();
                //console.log("Payload = "+payload);
                onMessageReceived(payload)
                readStream();
            });
        }
        readStream();
    }).catch(error => {
        console.error('Error:', error);
    });
    messageInput.value = '';
    onMessageReceived(msg)
}

function handleStream(event) {
    console.log(event.data);
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

function onWsMessageReceived(payload) {
    onMessageReceived(payload.body);
}

function onMessageReceived(payload) {
    try{
        //console.log(payload);
        var message = JSON.parse(payload);
        var textElement = document.getElementById(message.messageId);
        if(textElement){
            //console.log("element find")
            var messageText = document.createTextNode(message.content);
            textElement.appendChild(messageText);
            return;
        } else {
            //console.log("element NOT find")
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
    } catch(err) {
        console.log("Error Payload -> "+payload)
        console.error('Error:', err);
    }
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