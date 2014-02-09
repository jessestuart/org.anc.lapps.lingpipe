package org.anc.lapps.lingpipe;

import org.anc.lapps.serialization.Container;
import org.anc.resource.ResourceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Jesse Stuart
 */
public class SentenceSplitterTest
{
	private WebService service;

	@Before
	public void setup()
	{
		this.service = new SentenceSplitter();
	}

	@After
	public void tearDown()
	{
		this.service = null;
	}

	@Test
	public void testSentenceSplitter() throws IOException
	{
		this.service = new SentenceSplitter();
		String inputText = ResourceLoader.loadString("Bartok.txt");

		Data input = DataFactory.text(inputText);
		Data result = service.execute(input);
		long resultType = result.getDiscriminator();
		String payload = result.getPayload();

		assertTrue(payload, resultType != Types.ERROR);
		assertTrue("Expected JSON", resultType == Types.JSON);
		Container container = new Container(payload);
		System.out.println(container.toPrettyJson());
	}

}
