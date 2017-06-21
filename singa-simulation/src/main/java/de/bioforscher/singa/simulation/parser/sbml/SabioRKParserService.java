package de.bioforscher.singa.simulation.parser.sbml;

import de.bioforscher.singa.core.parser.rest.AbstractHTMLParser;
import de.bioforscher.singa.simulation.modules.reactions.implementations.DynamicReaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SabioRKParserService extends AbstractHTMLParser<List<DynamicReaction>> {

    private Map<String, String> queryMap;
    private static final String SABIORK_FETCH_URL = "http://sabiork.h-its.org/sabioRestWebServices/searchKineticLaws/sbml";

    public SabioRKParserService(String entryID) {
        setResource(SABIORK_FETCH_URL);
        this.queryMap = new HashMap<>();
        this.queryMap.put("q", entryID);
    }

    @Override
    public List<DynamicReaction> parse() {
        fetchWithQuery(this.queryMap);
        SBMLParser parser = new SBMLParser(getFetchResult());
        parser.parse();
        return parser.getReactions();
    }




}