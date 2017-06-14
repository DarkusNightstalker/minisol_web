/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



$(function () {

    "use strict";

    // Init Demo JS  
    Demo.init();

    // Init Admin Panels on widgets inside the ".admin-panels" container
    $('.admin-panels').adminpanel({
        grid: '.admin-grid',
        draggable: true,
        // preserveGrid: true,
        // mobile: true,
        onStart: function () {
            // Do something before AdminPanels runs
        },
        onFinish: function () {
            $('.admin-panels').addClass('animated fadeIn').removeClass('fade-onload');

            // Init the rest of the plugins now that the panels
            // have had a chance to be moved and organized.
            // It's less taxing to organize empty panels
            demoHighCharts.init();
            runVectorMaps(); // function below
        },
        onRemove: function (panel) {
            var pID = $(panel).attr('id');
            console.log(pID);
        }
    });


    // Init plugins for ".calendar-widget"
    // plugins: FullCalendar
    //
    $('#calendar-widget').fullCalendar({
        // contentHeight: 397,
        editable: true,
        events: [{
                title: 'Sony Meeting',
                start: '2015-06-1',
                end: '2015-06-3',
                className: 'fc-event-success',
            }, {
                title: 'Conference',
                start: '2015-06-11',
                end: '2015-06-13',
                className: 'fc-event-warning'
            }, {
                title: 'Lunch Testing',
                start: '2015-06-21',
                end: '2015-06-23',
                className: 'fc-event-primary'
            },
        ],
        eventRender: function (event, element) {
            // create event tooltip using bootstrap tooltips
            $(element).attr("data-original-title", event.title);
            $(element).tooltip({
                container: 'body',
                delay: {
                    "show": 100,
                    "hide": 200
                }
            });
            // create a tooltip auto close timer  
            $(element).on('show.bs.tooltip', function () {
                var autoClose = setTimeout(function () {
                    $('.tooltip').fadeOut();
                }, 3500);
            });
        }
    });


    // Init plugins for ".task-widget"
    // plugins: Custom Functions + jQuery Sortable
    //
    var taskWidget = $('div.task-widget');
    var taskItems = taskWidget.find('li.task-item');
    var currentItems = taskWidget.find('ul.task-current');
    var completedItems = taskWidget.find('ul.task-completed');

    // Init jQuery Sortable on Task Widget
    taskWidget.sortable({
        items: taskItems, // only init sortable on list items (not labels)
        handle: '.task-menu',
        axis: 'y',
        connectWith: ".task-list",
        update: function (event, ui) {
            var Item = ui.item;
            var ParentList = Item.parent();

            // If item is already checked move it to "current items list"
            if (ParentList.hasClass('task-current')) {
                Item.removeClass('item-checked').find('input[type="checkbox"]').prop('checked', false);
            }
            if (ParentList.hasClass('task-completed')) {
                Item.addClass('item-checked').find('input[type="checkbox"]').prop('checked', true);
            }

        }
    });

    // Custom Functions to handle/assign list filter behavior
    taskItems.on('click', function (e) {
        e.preventDefault();
        var This = $(this);
        var Target = $(e.target);

        if (Target.is('.task-menu') && Target.parents('.task-completed').length) {
            This.remove();
            return;
        }

        if (Target.parents('.task-handle').length) {
            // If item is already checked move it to "current items list"
            if (This.hasClass('item-checked')) {
                This.removeClass('item-checked').find('input[type="checkbox"]').prop('checked', false);
            }
            // Otherwise move it to the "completed items list"
            else {
                This.addClass('item-checked').find('input[type="checkbox"]').prop('checked', true);
            }
        }

    });


    var highColors = [bgSystem, bgSuccess, bgWarning, bgPrimary];

    // Chart data
    var seriesData = [{
            name: 'Phones',
            data: [5.0, 9, 17, 22, 19, 11.5, 5.2, 9.5, 11.3, 15.3, 19.9, 24.6]
        }, {
            name: 'Notebooks',
            data: [2.9, 3.2, 4.7, 5.5, 8.9, 12.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8]
        }, {
            name: 'Desktops',
            data: [15, 19, 22.7, 29.3, 22.0, 17.0, 23.8, 19.1, 22.1, 14.1, 11.6, 7.5]
        }, {
            name: 'Music Players',
            data: [11, 6, 5, 15, 17.0, 22.0, 30.8, 24.1, 14.1, 11.1, 9.6, 6.5]
        }];

    var ecomChart = $('#ecommerce_chart1');
    if (ecomChart.length) {
        ecomChart.highcharts({
            credits: false,
            colors: highColors,
            chart: {
                backgroundColor: 'transparent',
                className: '',
                type: 'line',
                zoomType: 'x',
                panning: true,
                panKey: 'shift',
                marginTop: 45,
                marginRight: 1,
            },
            title: {
                text: null
            },
            xAxis: {
                gridLineColor: '#EEE',
                lineColor: '#EEE',
                tickColor: '#EEE',
                categories: ['Jan', 'Feb', 'Mar', 'Apr',
                    'May', 'Jun', 'Jul', 'Aug',
                    'Sep', 'Oct', 'Nov', 'Dec'
                ]
            },
            yAxis: {
                min: 0,
                tickInterval: 5,
                gridLineColor: '#EEE',
                title: {
                    text: null,
                }
            },
            plotOptions: {
                spline: {
                    lineWidth: 3,
                },
                area: {
                    fillOpacity: 0.2
                }
            },
            legend: {
                enabled: true,
                floating: false,
                align: 'right',
                verticalAlign: 'top',
                x: -15
            },
            series: seriesData
        });
    }

    // Widget VectorMap
    function runVectorMaps() {

        // Jvector Map Plugin
        var runJvectorMap = function () {
            // Data set
            var mapData = [900, 700, 350, 500];
            // Init Jvector Map
            $('#WidgetMap').vectorMap({
                map: 'us_lcc_en',
                //regionsSelectable: true,
                backgroundColor: 'transparent',
                series: {
                    markers: [{
                            attribute: 'r',
                            scale: [3, 7],
                            values: mapData
                        }]
                },
                regionStyle: {
                    initial: {
                        fill: '#E8E8E8'
                    },
                    hover: {
                        "fill-opacity": 0.3
                    }
                },
                markers: [{
                        latLng: [37.78, -122.41],
                        name: 'San Francisco,CA'
                    }, {
                        latLng: [36.73, -103.98],
                        name: 'Texas,TX'
                    }, {
                        latLng: [38.62, -90.19],
                        name: 'St. Louis,MO'
                    }, {
                        latLng: [40.67, -73.94],
                        name: 'New York City,NY'
                    }],
                markerStyle: {
                    initial: {
                        fill: '#a288d5',
                        stroke: '#b49ae0',
                        "fill-opacity": 1,
                        "stroke-width": 10,
                        "stroke-opacity": 0.3,
                        r: 3
                    },
                    hover: {
                        stroke: 'black',
                        "stroke-width": 2
                    },
                    selected: {
                        fill: 'blue'
                    },
                    selectedHover: {}
                },
            });
            // Manual code to alter the Vector map plugin to 
            // allow for individual coloring of countries
            var states = ['US-CA', 'US-TX', 'US-MO',
                'US-NY'
            ];
            var colors = [bgInfo, bgPrimaryLr, bgSuccessLr, bgWarningLr];
            var colors2 = [bgInfo, bgPrimary, bgSuccess, bgWarning];
            $.each(states, function (i, e) {
                $("#WidgetMap path[data-code=" + e + "]").css({
                    fill: colors[i]
                });
            });
            $('#WidgetMap').find('.jvectormap-marker')
                    .each(function (i, e) {
                        $(e).css({
                            fill: colors2[i],
                            stroke: colors2[i]
                        });
                    });
        }

        if ($('#WidgetMap').length) {
            runJvectorMap();
        }
    }

});