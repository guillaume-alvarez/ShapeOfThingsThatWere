package com.galvarez.ttw.model.components;

import com.galvarez.ttw.model.data.Discovery;

import java.util.Objects;

public interface Card {

  class DiscoveryCard implements Card {

    public final Discovery discovery;

    public DiscoveryCard(Discovery discovery) {
      this.discovery = discovery;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DiscoveryCard that = (DiscoveryCard) o;
      return Objects.equals(discovery, that.discovery);
    }

    @Override
    public int hashCode() {
      return Objects.hash(discovery);
    }
  }

}
