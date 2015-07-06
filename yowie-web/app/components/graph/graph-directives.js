'use strict';

angular.module('yowieApp.graph.graph-directives', [])

    .directive('ywDependency', function () {

        return {

            restrict: 'AE',
            templateUrl: 'components/graph/dependency.html',
            scope: {
                context: '='
            },
            link: function (scope, element) {

                var groupContext = scope.context;

                if (groupContext.taskContexts.length <= 0) {
                    return
                }

                var getIndexForTask = function (taskName) {
                    return _.findIndex(groupContext.taskContexts, function (item) {
                        return item.task.name === taskName;
                    });
                };

                scope.links = [];

                _.forEach(groupContext.taskContexts, function (taskContext) {

                    if (taskContext.task.dependsOn != undefined && taskContext.task.dependsOn != null) {

                        scope.links.push({
                            source: getIndexForTask(taskContext.task.name),
                            target: getIndexForTask(taskContext.task.dependsOn.name),
                            value: 1
                        })
                    }
                });

                var width = '400';
                var height = groupContext.taskContexts.length * 80;

                var color = d3.scale.category10();
                var force = d3.layout.force().charge(-3000).linkDistance(20).size([width, height]);
                force.nodes(groupContext.taskContexts).links(scope.links).start();

                var svg = d3.select(element[0]).append("svg")
                    .attr("width", '100%')
                    .attr("height", height)
                    .attr("viewBox", "0 0 " + width + " " + height)
                    .attr("style", "border: 1px solid #000000;");

                svg.append("defs").selectAll("marker")
                    .data(["suit"])
                    .enter().append("marker")
                    .attr("id", function (d) {
                        return d;
                    })
                    .attr("viewBox", "0 -5 10 10")
                    .attr("refX", 15)
                    .attr("refY", -1.5)
                    .attr("markerWidth", 6)
                    .attr("markerHeight", 6)
                    .attr("orient", "auto")
                    .append("path")
                    .attr("d", "M0,-5L10,0L0,5");


                var paths = svg.selectAll(".links")
                    .data(scope.links)
                    .enter().append("line")
                    .style("stroke", "#ccc")
                    .style("stroke-width", 2)
                    .attr("marker-end", "url(#suit)");

                var nodes = svg.selectAll("g")
                    .data(groupContext.taskContexts)
                    .enter()
                    .append("g");

                var circles = nodes.append("circle")
                    .attr("r", 10)
                    .style("fill", function (d) {
                        return color(d.task.name);
                    })
                    .call(force.drag);

                var texts = nodes.append("text")
                    .attr("x", 15)
                    //.attr("y", 12)
                    .attr("dy", ".35em")
                    .attr("text-anchor", "right")
                    .text(function (d) {

                        var title = d.task.name;

                        if (d.task.dependsOn) {
                            
                            var status = d.task.dependsOn.status.value
                            
                            if(_.isUndefined(status)) {
                                status = d.task.dependsOn.status
                            }
                            
                            title = title + " (" + status + ")";
                        }

                        return title;
                    });

                function transform(d) {
                    return "translate(" + d.x + "," + d.y + ")";
                }

                force.on("tick", function () {

                    circles.attr("transform", transform);
                    texts.attr("transform", transform);

                    paths.attr("x1", function (d) {
                        return d.source.x;
                    })
                        .attr("y1", function (d) {
                            return d.source.y;
                        })
                        .attr("x2", function (d) {
                            return d.target.x;
                        })
                        .attr("y2", function (d) {
                            return d.target.y;
                        });
                });
            }
        }
    });
