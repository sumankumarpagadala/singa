package de.bioforscher.singa.chemistry.descriptive.features.databases.unichem;

import de.bioforscher.singa.chemistry.descriptive.features.databases.uniprot.UniProtParserService;
import de.bioforscher.singa.core.identifier.ChEBIIdentifier;
import de.bioforscher.singa.core.identifier.InChIKey;
import de.bioforscher.singa.core.identifier.PubChemIdentifier;
import de.bioforscher.singa.core.identifier.model.Identifier;
import de.bioforscher.singa.core.parser.rest.AbstractHTMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cl
 */
public class UniChemParser extends AbstractHTMLParser<List<Identifier>> {

    private static final Logger logger = LoggerFactory.getLogger(UniProtParserService.class);
    private static final String UNICHEM_FETCH_URL = "https://www.ebi.ac.uk/unichem/rest/verbose_inchikey/%s";

    public UniChemParser(InChIKey inChIKey) {
        setResource(String.format(UNICHEM_FETCH_URL, inChIKey.toString()));
    }

    public static List<Identifier> parse(InChIKey inChIKey) {
        UniChemParser parser = new UniChemParser(inChIKey);
        return parser.parse();
    }

    @Override
    public List<Identifier> parse() {
        fetchResource();
        try {
            try (InputStreamReader inputStreamReader = new InputStreamReader(getFetchResult())) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    return processLines(bufferedReader.lines().collect(Collectors.toList()));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not open input stream for uni chem resource.", e);
        }
    }

    private List<Identifier> processLines(List<String> lines) {
        boolean isChebi = false;
        boolean isPubChem = false;
        List<Identifier> identifiers = new ArrayList<>();
        for (String line : lines) {
            if (line.equals("  name: chebi")) {
                isChebi = true;
            } else if (line.equals("  name: pubchem")) {
                isPubChem = true;
            } else if (isChebi && line.startsWith("    - ")) {
                isChebi = false;
                String chebiNumber = line.substring(5).trim();
                identifiers.add(new ChEBIIdentifier("CHEBI:" + chebiNumber));
            } else if (isPubChem && line.startsWith("    - ")) {
                isPubChem = false;
                String pubChemNumber = line.substring(5).trim();
                identifiers.add(new PubChemIdentifier("CID:" + pubChemNumber));
            }
        }
        return identifiers;
    }

}