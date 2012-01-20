/*
  Draws a simple interactive world map that you can query.
  It uses the giCentre's geoMap library.
  Iain Dillingham, 20th January 2011.
*/
import org.gicentre.geomap.*;

GeoMap geoMap;

void setup()
{
  size(800, 400);
  smooth();
  geoMap = new GeoMap(this);
  geoMap.readFile("world");
  geoMap.getAttributes().writeAsTable(5);
}

void draw()
{
  background(180, 210, 240);
  stroke(0);
  fill(150, 190, 150);
  geoMap.draw();

  int id = geoMap.getID(mouseX, mouseY);
  if (id != -1)
  {
    fill(180, 120, 120);
    geoMap.draw(id);
  }

  noStroke();
  fill(255, 192);
  rect(0, 0, width, 20);

  if (id != -1)
  {
    String name = geoMap.getAttributes().getString(id, 4);
    fill(0);
    textAlign(LEFT, CENTER);
    text(name, 0, 0, width, 20);
  }
}

