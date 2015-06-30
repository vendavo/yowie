'use strict';

angular.module('yowieApp.group', [])

    .directive('ywGroupRunningInfo', function() {

        return {

            restrict: 'AE',
            templateUrl: 'components/group/running-info.html',
            scope: {
                groupContext: '='
            },
            link: function (scope, element) {

                scope.running = _.reduce(scope.groupContext.taskContexts, function (memo, tc) {

                    if(tc.running) {
                        return memo + 1;
                    }

                    return memo
                }, 0);
                
                scope.done = _.reduce(scope.groupContext.taskContexts, function (memo, tc) {
                    
                    if(tc.done) {
                        return memo + 1;
                    }
                    
                    return memo
                }, 0);

                scope.rest = _.reduce(scope.groupContext.taskContexts, function (memo, tc) {

                    if(!tc.done) {
                        return memo + 1;
                    }

                    return memo
                }, 0);
            }
        }
    });
