package org.acme.quarkus.sample;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;

@ExtendWith(MockitoExtension.class)
public class APriceResourceUnitTest {

  @Mock
  Publisher<Double> publisher;

  private PriceResource priceResource;

  @BeforeEach
  public void setup() {
    priceResource = new PriceResource(publisher);
  }

  @Test
  public void aTest() {
    Assert.assertEquals(publisher, priceResource.stream());
  }
}
