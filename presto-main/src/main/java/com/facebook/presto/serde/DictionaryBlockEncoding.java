package com.facebook.presto.serde;

import com.facebook.presto.block.Block;
import com.facebook.presto.block.dictionary.Dictionary;
import com.facebook.presto.block.dictionary.DictionaryEncodedBlock;
import com.facebook.presto.slice.SliceInput;
import com.facebook.presto.slice.SliceOutput;
import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.checkNotNull;

public class DictionaryBlockEncoding
        implements BlockEncoding
{
    private final Dictionary dictionary;
    private final BlockEncoding idBlockEncoding;

    public DictionaryBlockEncoding(Dictionary dictionary, BlockEncoding idBlockEncoding)
    {
        this.dictionary = checkNotNull(dictionary, "dictionary is null");
        this.idBlockEncoding = checkNotNull(idBlockEncoding, "idBlockEncoding is null");
    }

    public DictionaryBlockEncoding(SliceInput input)
    {
        dictionary = DictionarySerde.readDictionary(input);
        idBlockEncoding = BlockEncodings.readBlockEncoding(input);
    }

    public Dictionary getDictionary()
    {
        return dictionary;
    }

    public BlockEncoding getIdBlockEncoding()
    {
        return idBlockEncoding;
    }

    @Override
    public void writeBlock(SliceOutput sliceOutput, Block block)
    {
        DictionaryEncodedBlock dictionaryBlock = (DictionaryEncodedBlock) block;
        Preconditions.checkArgument(dictionaryBlock.getDictionary() == dictionary, "Block dictionary is not the same a this dictionary");
        idBlockEncoding.writeBlock(sliceOutput, dictionaryBlock.getIdBlock());
    }

    @Override
    public Block readBlock(SliceInput sliceInput, long positionOffset)
    {
        Block idBlock = idBlockEncoding.readBlock(sliceInput, positionOffset);
        return new DictionaryEncodedBlock(dictionary, idBlock);
    }

    public static void serialize(SliceOutput output, DictionaryBlockEncoding encoding)
    {
        DictionarySerde.writeDictionary(output, encoding.dictionary);
        BlockEncodings.writeBlockEncoding(output, encoding.idBlockEncoding);
    }
}