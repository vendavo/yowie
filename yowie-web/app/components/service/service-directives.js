'use strict';

angular.module('yowieApp.service.service-directive', [])

    .directive('ywCounter', function () {

        return {

            restrict: 'AE',
            templateUrl: 'components/service/counter.html',
            scope: {
                service: '='
            },
            link: function (scope, element) {

               scope.inProgress = function () {
                    return _.reduce(scope.service.getLastMessageHolder().value, function (memo, context) {

                        if (!context.done) {
                            return memo + 1;
                        }

                        return memo;

                    }, 0);
                };

                scope.finished = function () {
                    return _.reduce(scope.service.getLastMessageHolder().value, function (memo, context) {

                        if (context.done) {
                            return memo + 1;
                        }

                        return memo;

                    }, 0);
                };
            }
        }
    })
    .directive('ywStatus', function () {

        return {

            restrict: 'AE',
            templateUrl: 'components/service/status.html',
            scope: {
                service: '='
            },
            link: function (scope, element) {

                scope.reconnect = function () {
                    scope.service.reConnect();
                };

                scope.connected = scope.service.isConnected;
            }
        }
    });
