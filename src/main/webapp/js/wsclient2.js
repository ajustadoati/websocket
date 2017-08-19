var app = angular.module("app", []);

app.controller("MiController", ['$scope', '$log', '$http', function ($scope, $log, $http) {
    //$scope.enteredName = "david";

    $scope.empleados = [];
    $scope.enteredName = "";
    userName = null;
    $scope.funTest = function () {

        $http({
            method: 'GET',
            url: 'usuarios.json'
        }).success(function (data, status, headers, config) {

            //$scope.mensajes=data;
            $log.debug(data);

            $scope.empleados = data.departamentosSucursal[0].listaEmpleados;

            console.log("el valor del scope empleados es: " + $scope.empleados);
        }).error(function (data, status, headers, config) {
            alert("Ha fallado la petición. Estado HTTP:" + status);
        });
        console.log("voy a entrar al for");
        for (var i = 0; i < $scope.empleados.length; i++) {
            console.log("empleado:" + $scope.empleados[i].nombreUsuario);
            if ($scope.enteredName == $scope.empleados[i].nombreUsuario) {
                console.log("usuario valido llamando a websocket");
                //userName = $scope.enteredName;
                $scope.wsclient.connect($scope.enteredName);
                return;

            }
        }
    }


    //-----------------------------------------------------------------------------------------
    $scope.wsclient = (function () {

        var ws = null;
        var wsURI = 'wss://' + location.host + '/websockets/chat';

        function connect(userName) {

            if (!userName || userName == '') { //compara si el userName esta vacio
                return;
            }

            if ('WebSocket' in window) {
                ws = new WebSocket(wsURI + '?userName=' + userName);
            } else if ('MozWebSocket' in window) {
                ws = new MozWebSocket(wsURI + '?userName=' + userName);
            } else {
                alert('Tu navegador no soporta WebSockets');
                return;
            }
            ws.onopen = function () { //abre la conexion
                setConnected(true);
            };
            ws.onmessage = function (event) { //crea el evento para mandar los datos 
                var message = JSON.parse(event.data); //se hace el parse desde json
                processMessage(message); //se envia el json como la variable message
            };

            ws.onclose = function () {
                setConnected(false);
                document.getElementById('userName').value = '';
                var userName = '';
                closeAllConversations();
            };

            function processMessage(message) { //procesa los mensajes que vienen del metodo onmessage del backend
                if (message.messageInfo) { //verifica si el mensaje proviene de la clase messageInfo (FLUJO CONSTANTE POR CADA MENSAJE ENVIADO)
                    showConversation(message.messageInfo.from); //se prepara para verificar si crea o no el panel de conversacion
                    addMessage(message.messageInfo.from, message.messageInfo.message, cleanWhitespaces(message.messageInfo.from) + 'conversation'); //se agrega el mensaje el panel de conversacion
                } else if (message.statusInfo) { //verifica si el mensaje proviene de la clase statusInfo (FLUJO CONSTANTE AL CON Y DESCON)
                    if (message.statusInfo.status == 'CONNECTED') { //si el enum de la clase statusInfo CONNECTED se agrega un usuario online
                        addOnlineUser(message.statusInfo.user);
                    } else if (message.statusInfo.status == 'DISCONNECTED') { //si el enum es DISCONNECTED se quita un el usuario online
                        removeOnlineUser(message.statusInfo.user);
                    }
                } else if (message.connectionInfo) { //verifica si el mensaje proviene de la clase connectionInfo (SOLO AL CONECTAR)
                    var activeUsers = message.connectionInfo.activeUsers; //se trae todos los usuarios activos 
                    for (var i = 0; i < activeUsers.length; i++) { //busca en todos los usuarios del objetos
                        addOnlineUser(activeUsers[i]); //va agregando a los usuarios que encontro activos
                    }
                }
            }


        }

        function disconnect() { //metodo que desconecta la comunicacion con el backend
            if (ws != null) {
                ws.close();
                ws = null;
                $scope.enteredName = "";
            }
            setConnected(false);
        }

        function setConnected(connected) { // verifica la conexion de usuarios para agregar o quitar del estado de conexion
            document.getElementById('connect').disabled = connected;
            document.getElementById('disconnect').disabled = !connected;
            cleanConnectedUsers();
            if (connected) {
                updateUserConnected();
            } else {
                updateUserDisconnected();
            }
        }

        function updateUserConnected() { //actualiza usuarios conectados interactuando con CSS
            var inputUsername = $('#userName');
            var onLineUserName = $('.onLineUserName');
            onLineUserName.html(inputUsername.val());
            inputUsername.css({
                display: 'none'
            });
            onLineUserName.css({
                visibility: 'visible'
            });
            $('#status').html('Conectado');
            $('#status').attr({
                class: 'connected'
            });
            $('#onLineUsersPanel').css({
                visibility: 'visible'
            });
        }

        function updateUserDisconnected() { // actualiza usuario desconectado usando CSS
            $('.onLineUserName').css({
                visibility: 'hidden'
            });
            $('#userName').css({
                display: ''
            });
            $('#status').html('Desconectado');
            $('#status').attr({
                class: 'disconnected'
            });
            $('#onLineUsersPanel').css({
                visibility: 'hidden'
            });
        }

        function cleanConnectedUsers() {
            $('#onlineUsers').html('');
        }

        function removeTab(conversationId) {
            $('#conversations').tabs('remove', conversationId);
        }

        function cleanWhitespaces(text) {
            return text.replace(/\s/g, "_");
        }

        function showConversation(from) { //crea el panel de chat 
            console.log("en showConversation from es: " + from);
            var conversations = $('#conversations'); //crea la variable conversations 
            console.log("en showConversation conversations es: " + conversations);
            conversations.css({ //se le cambia el atributo visibility del css a visible para que muestre la conversacion
                visibility: 'visible'
            });
            var conversationId = cleanWhitespaces(from) + 'conversation';
            console.log("en showConversation conversationId es: " + conversationId);
            if (document.getElementById(conversationId) == null) { //si la conversacion no existe creal el panel
                createConversationPanel(from);
                conversations.tabs('add', '#' + conversationId, from);
            }
            conversations.tabs('select', '#' + conversationId);
            $('#' + conversationId + 'message').focus();
        }

        function createConversationPanel(name) {
            var conversationId = cleanWhitespaces(name) + 'conversation';
            console.log("en createConversation conversationId es: " + conversationId);
            var conversationPanel = $(document.createElement('div'));
            conversationPanel.attr({
                id: conversationId,
                class: 'conversation'
            });
            $('<p class="messages"></p><textarea id="' + conversationId + 'message"></textarea>').appendTo(conversationPanel);
            var sendButton = createSendButton(name);
            sendButton.appendTo(conversationPanel);
            var closeButton = createCloseButton(cleanWhitespaces(name));
            closeButton.appendTo(conversationPanel);
            conversationPanel.appendTo($('#conversations'));
        }

        function createSendButton(name) { //ejecuta el metodo que inicia proceso de envio los datos del msj 
            var conversationId = cleanWhitespaces(name) + 'conversation';
            var button = $(document.createElement('button')); //se crea un objeto boton, de tipo boton
            button.html('Enviar'); //se le agrega al objeto un nombre enviar
            button.click(function () { //se le da funcion al boton 
                var from = document.getElementById('userName').value; //se toma el valor de userName y se guarda en from
                var message = document.getElementById(conversationId + 'message').value;
                console.log("el conversationId + message en createSendButton es: " + message);
                $log.debug($scope.enteredName);
                toChat($scope.enteredName, name, message); //settea en toChat from, name y message para el backend
                addMessage(from, message, conversationId);
                document.getElementById(conversationId + 'message').value = '';
                console.log("el conversationId + message al final de createSendButton es: " + message);
            });
            return button;
        }

        function closeAllConversations() {
            for (var i = $('#conversations').tabs('length'); i >= 0; i--) {
                $('#conversations').tabs('remove', i - 1);
            }
            $('#conversations').css({
                visibility: 'hidden'
            });
        }

        function createCloseButton(conversationId) {
            var button = $(document.createElement('button'));
            button.html('Cerrar');
            button.click(function () {
                removeTab(conversationId);
            });
            return button;
        }

        function addMessage(from, message, conversationPanelId) { //agrega los mensajes al textbox del chat
            var messages = $('#' + conversationPanelId + ' .messages');
            $('<div class="message"><span><b>' + from + '</b> dice:</span><p>' + $('<p/>').text(message).html() + '</p></div>').appendTo(messages); //concatena las etiquetas html para mostrarlas 
            messages.scrollTop(messages[0].scrollHeight);
            $('#' + conversationPanelId + ' textarea').focus();
        }

        function toChat(sender, receiver, message) { //metodo que envia al backend origen, destino y mensaje
            ws.send(JSON.stringify({ //ws.send llama al metodo send para enviar informacion al back end
                messageInfo: {
                    from: sender,
                    to: receiver,
                    message: message
                }
            }));
        }

        /********* usuarios conectados *******/
        function addOnlineUser(userName) { //agrega un usuario online al front end 
            var newOnlineUser = createOnlineUser(userName); //se crea un nuevo usuario online con el nombre userName
            newOnlineUser.appendTo($('#onlineUsers')); //appendTo agrega un elemento a la lista de usuario online
        }

        function removeOnlineUser(userName) { //se remueven de la lista del front end el usuario userName 
            $('#onlineUsers > li').each(function (index, elem) {
                if (elem.id == userName + 'onlineuser') {
                    $(elem).remove();
                }
            });
        }

        function createOnlineUser(userName) { //crea los botones de los usuarios online
            var link = $(document.createElement('a')); //document.createElement crea un boton 
            link.html(userName);
            link.click(function () {
                showConversation(userName);
            });
            var li = $(document.createElement('li'));
            li.attr({
                id: (userName + 'onlineuser')
            });
            link.appendTo(li);
            return li;
        }

        // metodos publicos
        return {
            connect: connect,
            disconnect: disconnect
        };
    })();

    }]);