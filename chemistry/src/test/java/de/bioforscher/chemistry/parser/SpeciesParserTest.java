package de.bioforscher.chemistry.parser;

import de.bioforscher.chemistry.descriptive.Species;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Christoph on 19.04.2016.
 */
public class SpeciesParserTest {

    @Test
    public void shouldParseMethanolFromPubChemXML() {
        String resource = Thread.currentThread().getContextClassLoader().getResource("methanol-pubchem.xml").toString();
        PubChemParserService parser = new PubChemParserService(resource);
        Species methanol = parser.fetchSpecies();
        assertEquals("methanol", methanol.getName().toLowerCase());
        assertEquals(32.04186, methanol.getMolarMass().getValue().doubleValue(), 0.0);
    }

    @Test
    public void shouldParseMethanolFromChEBIOnline() {
        ChEBIParserService parser = new ChEBIParserService("CHEBI:17790");
        Species methanol = parser.fetchSpecies();
        assertEquals("methanol", methanol.getName().toLowerCase());
        assertEquals(32.04186, methanol.getMolarMass().getValue().doubleValue(), 0.0);
    }

    @Test
    public void shouldSearchMethanolInChEBIDatabase() {
        ChEBISearchService service = new ChEBISearchService();
        service.setSearchTerm("Methanol");
        List<Species> searchResult = service.search();
        assertEquals(20, searchResult.size());
        for (Species species : searchResult) {
            assertTrue(species != null);
        }
    }

    @Test
    public void shouldFetchImageForMethanolFromChEBIDatabase() throws IOException {
        ChEBIImageService service = new ChEBIImageService("CHEBI:17790");
        service.fetchResource();
        assertTrue(service.getImageStream() != null);
    }

}