'use strict';

angular.module('yowieApp.date.date-directive', [])

    .directive('ywDate', function() {
        
        return {

            restrict: 'AE',
            templateUrl: 'components/date/date.html',
            scope: {
                time: '=',
                dateTime: '='
            },
            link: function (scope, element) {
                
                if(scope.dateTime) {
                    scope.dateTime = new Date(scope.dateTime);    
                }
                
                if(scope.time) {
                    scope.time = new Date(scope.time);    
                }
            }
        }
    })
    .directive('ywTimeFromFirstTillNow', function() {

        return {

            restrict: 'AE',
            templateUrl: 'components/date/time-from-first-till-now.html',
            scope: {
                groupContext: '='
            },
            link: function (scope, element) {

                var taskContext = _.first(scope.groupContext.taskContexts);

                scope.startTime = taskContext.startTime;
                
                scope.duration = moment.utc(taskContext.startTime).toNow(true);
            }
        }
    });
