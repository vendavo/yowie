var resources = angular.module("yowie.resources", []);

resources.factory('TaskRestService', ['$resource',
    function ($resource) {

        var rest = $resource(YOWIE_BACKEND_URL + '/tasks/:id', {}, {
            query: {method: 'GET', params: {}, isArray: true},
            kill: {method: 'DELETE', params: {}}
        });
        
        var finishedRest = $resource(YOWIE_BACKEND_URL + '/tasks/status/finished', {}, {
            query: {method: 'GET', params: {page: '@page', size: '@size'}, isArray: true}
        });

        var service = {};

        service.getTasks = function (params) {
            return rest.query(params);
        };

        service.getFinishedTasks = function (params) {
            return finishedRest.query(params);
        };

        service.kill = function (id) {
            rest.kill({id: id});
        };

        return service;
    }]);

resources.factory('GroupRestService', ['$resource',
    function ($resource) {

        var rest = $resource(YOWIE_BACKEND_URL + '/groups', {}, {
            query: {method: 'GET', params: {page: '@page', size: '@size'}, isArray: true}
        });

        var finishedRest = $resource(YOWIE_BACKEND_URL + '/groups/status/finished', {}, {
            query: {method: 'GET', params: {page: '@page', size: '@size'}, isArray: true}
        });

        var service = {};

        service.getGroups = function (params) {
            return rest.query(params);
        };

        service.getFinishedGroups = function(params) {
            return finishedRest.query(params);
        }

        return service;
    }]);
