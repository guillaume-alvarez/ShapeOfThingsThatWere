var width = 960, height = 700;
var outer = d3.select("body").append("svg")
    .attr('width','100%')
    .attr('height','100%')
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
var toIndex = new Map(nodes.map((d, i) => [d.id, i]));
var edges = links.map(d => Object.assign(Object.create(d), {
    source: index.get(d.source),
    target: index.get(d.target),
    strong: d.strong
}));

// needed to index children on same level
parentNodes = new Map();
edges.forEach(function(edge) {
  if (edge.strong) {
      var sourceIndex = toIndex.get(edge.source.id);
      var targetIndex = toIndex.get(edge.target.id);
      if (parentNodes.get(sourceIndex) == null) {
        parentNodes.set(sourceIndex, []);
      }
      parentNodes.get(sourceIndex).push(targetIndex);
  }
});

var constraints = [];
parentNodes.forEach(function(children, parent, map) {
    // if there is a single child, keep it on same row
    if (children.length == 1) {
        constraints.push({
             'type': 'alignment',
             'axis': 'y',
             'offsets': [{node: parent, offset: 0}, {node: children[0], offset: 0}]
        });
    }
    // else all children is on the same level
    else {
        var constraint = {
                'type': 'alignment',
                'axis': 'x',
                'offsets': []};
        children.forEach(function(child) {
            constraint.offsets.push({node: child, offset: 0});
        });
        constraints.push(constraint);
    }
    // in any case keep children on the right of parents
    children.forEach(function(child) {
        constraints.push({"axis":"x", "left":parent, "right":child, "gap":25});
    });
});

// also set all root techs on same level
constraints.push({
    'type': 'alignment',
    'axis': 'x',
    'offsets': nodes.filter(n => n.root).map(function(n) { return {node: toIndex.get(n.id), offset: 0}; }),
});

size = outer.node().getBBox();
var d3cola = cola.d3adaptor(d3)
    .avoidOverlaps(true)
    .convergenceThreshold(1e-3)
    .flowLayout('x', 300)
    .avoidOverlaps(true)
    .size([size.width, size.height])
    .nodes(nodes)
    .links(edges)
    .constraints(constraints)
    .symmetricDiffLinkLengths(50);

var link = vis.selectAll(".link")
    .data(edges)
    .enter().append("path")
    .attr("class", "link")
    .attr("class", function (d) {
            return d.strong ? "link strong": "link light";
        });

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

// Add the tooltip
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
        return lineFunction(d3cola.routeEdge(d));
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