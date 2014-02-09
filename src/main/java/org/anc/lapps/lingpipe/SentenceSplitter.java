package org.anc.lapps.lingpipe;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
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

import java.util.Iterator;
import java.util.Set;

/**
 * @author Jesse Stuart
 */
public class SentenceSplitter implements WebService
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
		step.getMetadata().put(Metadata.PRODUCED_BY, "Lingpipe Sentence Splitter");

		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		SentenceModel sentenceModel = new MedlineSentenceModel();
		SentenceChunker sentenceChunker = new SentenceChunker(tokenizerFactory, sentenceModel);

		Chunking chunking = sentenceChunker.chunk(text.toCharArray(), 0, text.length());
		Set<Chunk> sentences = chunking.chunkSet();

		if (sentences.size() < 1)
		{
			return DataFactory.error("No sentence chunks found.");
		}

		String slice = chunking.charSequence().toString();
		int i = 1;
		IDGenerator id = new IDGenerator();
		for (Iterator<Chunk> it = sentences.iterator(); it.hasNext(); )
		{
			Chunk sentence = it.next();
			int start = sentence.start();
			int end = sentence.end();

			Annotation a = new Annotation();
			a.setId(id.generate("s"));
			a.setStart(start);
			a.setEnd(end);
			a.setLabel(Annotations.SENTENCE);

			step.addAnnotation(a);
		}

		container.getSteps().add(step);
		data = DataFactory.json(container.toJson());

		return data;
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

	public SentenceSplitter()
	{
		logger.info("Lingpipe Sentence Splitter created.");
	}

	@Override
	public long[] requires()
	{
		return new long[]{Types.TEXT};
	}

	@Override
	public long[] produces()
	{
		return new long[]{Types.JSON, Types.SENTENCE};
	}

	@Override
	public Data configure(Data data)
	{
		return DataFactory.error("Unsupported operation.");
	}
}
