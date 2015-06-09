'use strict';

var tasksModule = angular.module('yowie.tasks', ['yowie.services', 'yowie.resources']);

tasksModule.controller('TaskController', ['$scope', '$modal', 'GroupService', 'TaskService', function($scope, $modal, groupService, taskService) {
    $scope.groupContexts = groupService.getLastMessageHolder();

    $scope.kill = function (taskContext) {
        taskService.kill(taskContext);
    };

    $scope.showDependencyGraph = function (groupContext) {

        var modalInstance = $modal.open({
            animation: true,
            controller: 'DependencyGraphController',
            templateUrl: 'taskView/dependencyGraph.html',
            size: 'lg',
            resolve: {
                context: function () {
                    return groupContext;
                }
            }
        });
    };
}]);

tasksModule.controller('DependencyGraphController', ['$scope', '$modalInstance', 'context', function ($scope, $modalInstance, context) {

    $scope.context = context;
    
}]);
