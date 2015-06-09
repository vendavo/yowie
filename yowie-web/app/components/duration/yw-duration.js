'use strict';

angular.module('yowieApp.filter.moment', []).filter('moment', function () {
    
    return function(durationValue) {
        
        var duration = moment.duration(durationValue)

        return duration.humanize() + " (" + Math.floor(duration.asSeconds()) + "s)";  
    }
});
