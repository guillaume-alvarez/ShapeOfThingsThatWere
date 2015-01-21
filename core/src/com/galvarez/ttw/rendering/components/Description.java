package com.galvarez.ttw.rendering.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public final class Description extends Component {

  public String desc;

  public final AtlasRegion texture;

  public Description(String desc) {
    this(desc, null);
  }

  public Description(String desc, AtlasRegion texture) {
    this.desc = desc;
    this.texture = texture;
  }

  @Override
  public String toString() {
    return desc;
  }

}
