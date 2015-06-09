'use strict';

angular.module('yowieApp.event.event-directive', [])

    .directive('ywEvent', function() {

        return {

            restrict: 'AE',
            templateUrl: 'components/event/event.html',
            scope: {
                context: '='
            },
            link: function (scope, element) {

            }
        }
    });
