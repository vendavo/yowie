'use strict';

var statusModule = angular.module('yowie.status', ['yowie.services']);

statusModule.controller('StatusController', ['$scope', 'ResourceService', function($scope, resourceService) {

    $scope.availableResources = resourceService.getLastMessageHolder();
}]);
