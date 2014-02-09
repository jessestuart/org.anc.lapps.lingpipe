package org.anc.lapps.lingpipe;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import org.anc.lapps.serialization.Annotation;
import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.util.IDGenerator;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jdstuart
 * Date: 2/9/14
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Tokenizer implements WebService
{
	private static final Logger logger = LoggerFactory.getLogger(SentenceSplitter.class);

	@Override
	public Data execute(Data input)
	{
		Container container = createContainer(input);
		if (container == null)
		{
			return input;
		}
		Data data = null;
		String text = container.getText();

		ProcessingStep step = new ProcessingStep();
		step.getMetadata().put(Metadata.PRODUCED_BY, "Lingpipe Tokenizer");

		TokenizerFactory tokFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		com.aliasi.tokenizer.Tokenizer tokenizer = tokFactory.tokenizer(text.toCharArray(), 0, text.toCharArray().length);
		String token;

		IDGenerator id = new IDGenerator();

		while ((token = tokenizer.nextToken()) != null)
		{
			logger.info("Token : {}", token);
			Annotation a = new Annotation();
			a.setId(id.generate("tok"));
			a.setLabel(Annotations.TOKEN);
			a.setStart(tokenizer.lastTokenStartPosition());
			a.setEnd(tokenizer.lastTokenEndPosition());

			Map features = a.getFeatures();
			features.put("word", token);

			step.addAnnotation(a);
		}

		container.getSteps().add(step);
		data = DataFactory.json(container.toJson());

		return data;  //To change body of implemented methods use File | Settings | File Templates.
	}

	protected Container createContainer(Data input)
	{
		Container container = null;
		long inputType = input.getDiscriminator();
		if (inputType == Types.ERROR)
		{
			return null;
		}
		else if (inputType == Types.TEXT)
		{
			container = new Container();
			container.setText(input.getPayload());
		}
		else if (inputType == Types.JSON)
		{
			container = new Container(input.getPayload());
		}
		return container;
	}

	@Override
	public long[] requires()
	{
		return new long[]{Types.TEXT};
	}

	@Override
	public long[] produces()
	{
		return new long[]{Types.JSON, Types.TOKEN};
	}

	@Override
	public Data configure(Data data)
	{
		return DataFactory.error("Unsupported operation.");
	}

	public static void main(String[] args)
	{
		System.out.println("Hello world");
	}
}
