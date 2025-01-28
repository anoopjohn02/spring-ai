'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

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
    }
    event.preventDefault();
}

function send(event) {
    sendStream(event);
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
    const streamUrl = host + '/v1/stream/api';
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

function onMessageReceived(payload) {
    try{
        //var data = JSON.stringify(payload);
        var message = JSON.parse(payload);
        var textElement = document.getElementById(message.messageId);
        if(textElement){
            var messageText = document.createTextNode(message.content);
            textElement.appendChild(messageText);
            return;
        } else {
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