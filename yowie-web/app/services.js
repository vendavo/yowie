var services = angular.module("yowie.services", ['yowie.resources']);

function AbstractService(socketUrl, topic, $q, $timeout, $rootScope, mapToHolderValue) {

    var service = {};
    var listener = $q.defer();
    var socket = {
        client: null,
        stomp: null
    };

    var holder = {
        value: undefined
    };

    var connected = false;

    service.RECONNECT_TIMEOUT = 30000;
    service.socketUrl = socketUrl;
    service.topic = topic;

    var setConnected = function (value) {
        connected = value;
        $rootScope.$apply();
    };

    service.receive = function () {
        return listener.promise;
    };

    service.isConnected = function () {
        return connected;
    };

    service.getLastMessageHolder = function () {
        return holder;
    };

    service.setMessageHolderValue = function (value) {
        holder.value = value;
    };

    var reconnect = function () {

        setConnected(false);

        $timeout(function () {
            service.initialize();
        }, this.RECONNECT_TIMEOUT);
    };

    var getMessage = function (data) {

        mapToHolderValue(holder, data)

        $rootScope.$apply();

        return holder.value;
    };

    var startListener = function () {

        setConnected(true);

        if (service.initFunction) {
            service.initFunction();
        }

        socket.stomp.subscribe(service.topic, function (data) {
            listener.notify(getMessage(data.body));
        });
    };

    service.initialize = function (initialHolderValue, initFunction) {
        socket.client = new SockJS(YOWIE_BACKEND_URL + service.socketUrl);
        socket.stomp = Stomp.over(socket.client);
        socket.stomp.connect({}, startListener, function () {
            setConnected(false)
        });
        socket.stomp.onclose = reconnect;

        holder.value = initialHolderValue;

        service.initFunction = initFunction;
    };

    service.reConnect = function () {
        service.initialize();
    };

    return service;
};


services.service("ResourceService", function ($q, $timeout, $rootScope) {

    var service = new AbstractService("/ws/status", "/topic/status/resources", $q, $timeout, $rootScope, function (holder, data) {

        data.offers = _.sortBy(data.offers, 'id');
        holder.value = data;
    });
    service.initialize({});

    return service;
});

services.service("TaskService", function ($q, $timeout, $rootScope, TaskRestService) {

    var service = new AbstractService("/ws/tasks", "/topic/tasks", $q, $timeout, $rootScope, function (tasksHolder, taskContext) {

        var tasks = tasksHolder.value;

        var index = _.findIndex(tasks, function (item) {
            return item.task.id === taskContext.task.id;
        });

        if (index === -1) {
            tasks.push(taskContext);
        } else {
            tasks[index] = taskContext;
        }
    });

    service.initialize([], function () {
        service.setMessageHolderValue(TaskRestService.getTasks());
    });

    service.kill = function (taskContext) {
        TaskRestService.kill(taskContext.task.id);
    };

    return service;
});

services.service("GroupService", function ($q, $timeout, $rootScope, GroupRestService) {

    var service = new AbstractService("/ws/groups", "/topic/groups", $q, $timeout, $rootScope, function (groupsHolder, groupContext) {

        var groups = groupsHolder.value;

        var index = _.findIndex(groups, function (item) {
            return item.group.id === groupContext.group.id;
        });

        if (index === -1) {
            groups.push(groupContext);
        } else {
            groups[index] = groupContext;
        }
    });
    service.initialize([], function () {
        service.setMessageHolderValue(GroupRestService.getGroups());
    });

    return service;
});

services.run(function (TaskService, GroupService) {
    console.log('TaskService and GroupService are ready!');
});
