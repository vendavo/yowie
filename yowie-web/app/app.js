'use strict';

angular.module('yowieApp', [
    'ngRoute',
    'ngResource',
    'ui.bootstrap',
    'yowie.status',
    'yowie.tasks',
    'yowie.resources',
    'yowie.services',
    'yowieApp.date.date-directive',
    'yowieApp.event.event-directive',
    'yowieApp.service.service-directive',
    'yowieApp.graph.graph-directives',
    'yowieApp.filter.moment'
]).
    config(['$routeProvider', function ($routeProvider) {

        $routeProvider.when('/status', {
            templateUrl: 'statusView/status.html',
            controller: 'StatusController'
        }).when('/tasks', {
            templateUrl: 'taskView/task.html',
            controller: 'TaskController'
        }).otherwise({redirectTo: '/status'});
    }])
    .controller('MenuController', ['$scope', 'ResourceService', 'GroupService', function ($scope, resourceService, groupService) {

        $scope.availableResources = resourceService.getLastMessageHolder();

        $scope.services = {
            resource: resourceService,
            group: groupService
        };
    }]);
