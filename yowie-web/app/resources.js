var resources = angular.module("yowie.resources", []);

resources.factory('TaskRestService', ['$resource',
    function ($resource) {

        var rest = $resource(YOWIE_BACKEND_URL + '/tasks/:id', {}, {
            query: {method: 'GET', params: {}, isArray: true},
            kill: {method: 'DELETE', params: {}}
        });
        
        var finishedRest = $resource(YOWIE_BACKEND_URL + '/tasks/status/finished', {}, {
            query: {method: 'GET', params: {}, isArray: true}
        });

        var service = {};

        service.getTasks = function (callback) {
            return rest.query(callback);
        };

        service.getFinishedTasks = function () {
            return finishedRest.query();
        };

        service.kill = function (id) {
            rest.kill({id: id});
        };

        return service;
    }]);

resources.factory('GroupRestService', ['$resource',
    function ($resource) {

        var rest = $resource(YOWIE_BACKEND_URL + '/groups', {}, {
            query: {method: 'GET', params: {}, isArray: true}
        });

        var finishedRest = $resource(YOWIE_BACKEND_URL + '/groups/status/finished', {}, {
            query: {method: 'GET', params: {}, isArray: true}
        });

        var service = {};

        service.getGroups = function (callback) {
            return rest.query(callback);
        };

        service.getFinishedGroups = function() {
            return finishedRest.query();
        }

        return service;
    }]);
