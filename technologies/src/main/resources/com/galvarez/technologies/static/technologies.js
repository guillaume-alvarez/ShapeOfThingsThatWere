var width = 960, height = 700;
var outer = d3.select("body").append("svg")
    .attr('width',width)
    .attr('height',height)
    .attr('pointer-events',"all");

outer.append('rect')
    .attr('class','background')
    .attr('width',"100%")
    .attr('height',"100%")
    .call(d3.zoom().on("zoom", redraw));

var vis = outer
    .append('g')
    .attr('transform', 'translate(250,250) scale(0.3)');

function redraw() {
    vis.attr("transform", d3.event.transform);
}

outer
  .append('svg:defs')
  .append('svg:marker')
    .attr('id','end-arrow')
    .attr('viewBox','0 -5 10 10')
    .attr('refX',8)
    .attr('markerWidth',6)
    .attr('markerHeight',6)
    .attr('orient','auto')
  .append('svg:path')
    .attr('d','M0,-5L10,0L0,5L2,0')
    .attr('stroke-width','0px')
    .attr('fill','#000');

// Define the div for the tooltip
var tooltip = d3.select("body").append("div")
    .attr("class", "tooltip")
    .style("opacity", 0);


var index = new Map(nodes.map(d => [d.id, d]));
var edges = links.map(d => Object.assign(Object.create(d), {
    source: index.get(d.source),
    target: index.get(d.target)
}));

var d3cola = cola.d3adaptor(d3)
    .avoidOverlaps(true)
    .convergenceThreshold(1e-3)
    .flowLayout('x', 150)
    .size([width, height])
    .nodes(nodes)
    .links(edges)
    .jaccardLinkLengths(150);

var link = vis.selectAll(".link")
    .data(edges)
    .enter().append("path")
    .attr("class", "link");

var margin = 10, pad = 12;
var node = vis.selectAll(".node")
    .data(nodes)
    .enter().append("rect")
    .classed("node", true)
    .attr('rx',5)
    .attr('ry',5)
    .call(d3cola.drag);

var label = vis.selectAll(".label")
    .data(nodes)
    .enter().append("text")
    .attr("class", "label")
    .text(function (d) { return d.name; })
    .call(d3cola.drag)
    .each(function (d) {
        var b = this.getBBox();
        var extra = 2 * margin + 2 * pad;
        d.width = b.width + extra;
        d.height = b.height + extra;
    });

// Add the scatterplot
vis.selectAll(".label")
    .on("mouseover", function(d) {
        tooltip.transition()
            .duration(200)
            .style("opacity", .9);
        tooltip.html(d.text.replace(/(?:\r\n|\r|\n)/g, '<br>'))
            .style("left", (d3.event.pageX) + "px")
            .style("top", (d3.event.pageY - 28) + "px");
        })
    .on("mouseout", function(d) {
        tooltip.transition()
            .duration(500)
            .style("opacity", 0);
    });

var lineFunction = d3.line()
    .x(function (d) { return d.x; })
    .y(function (d) { return d.y; });

var routeEdges = function () {
    d3cola.prepareEdgeRouting();
    link.attr("d", function (d) {
        return lineFunction(d3cola.routeEdge(d
         // show visibility graph
            //, function (g) {
            //    if (d.source.id === 10 && d.target.id === 11) {
            //    g.E.forEach(function (e) {
            //        vis.append("line").attr("x1", e.source.p.x).attr("y1", e.source.p.y)
            //            .attr("x2", e.target.p.x).attr("y2", e.target.p.y)
            //            .attr("stroke", "green");
            //    });
            //    }
            //}
));
    });
}
d3cola.start(50, 100, 200).on("tick", function () {
    node.each(function (d) { d.innerBounds = d.bounds.inflate(-margin); })
        .attr("x", function (d) { return d.innerBounds.x; })
        .attr("y", function (d) { return d.innerBounds.y; })
        .attr("width", function (d) {
            return d.innerBounds.width();
        })
        .attr("height", function (d) { return d.innerBounds.height(); });

    link.attr("d", function (d) {
        var route = cola.makeEdgeBetween(d.source.innerBounds, d.target.innerBounds, 5);
        return lineFunction([route.sourceIntersection, route.arrowStart]);
    });

    label
        .attr("x", function (d) { return d.x })
        .attr("y", function (d) { return d.y + (margin + pad) / 2 });
});