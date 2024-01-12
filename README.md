# Introduktion

Her kan du finde information og kodeeksempler på anvendelse af XDS infrastruktur i forhold til deling af cda dokumenter. Eksemplerne er skrevet i Java.

Først gennemgåes de centrale koncepter i forhold til dokumentdeling dvs. de grundlæggende egenskaber ved CDA formatet og infrastrukturen, der anvendes til deling (XDS). Dernæst gennemgåes de relevante web services og der gives eksempler på anvendelse i form af (pseudo-Java)kode samt et komplet java eksempler forklares. Til sidst gives en oversigt over, hvilke endpoints, der er mulige at anvende i forbindelse med test af deling af dokumenter samt en liste over praktiske værktøjer i forbindelse med aftestning.

For nuværende er følgende typer af CDA dokumenter tilgængelig som fuldt kørende eksempler i nærværende kildekode:
* Personal Health Monitoring Report (PHMR) på dansk hjemmemonitorering
* Questionnaire Form Definition Document (QFDD) på dansk spørgeskema



# Hvordan beskrives dokumenter: CDA dokumenter
Data i en XDS infrastruktur gemmes som dokumenter. Et dokument har et unikt id (documentId) og en række metadata, der beskriver, hvad dokumentet handler om.
Dokumenter kan indholdsmæssigt være af flere forskellige typer: PDF, Word-dokument, men kan også være af typen CDA (Clinical Document Architecture). 
Et CDA dokument er egentlig bare et struktureret XML dokument, der følger en bestemt standard for kliniske dokumenter til udveksling (deling). 
Se f.eks. [What is HL7 CDA?] (http://iehr.eu/knowledge/what-is-hl7-cda/) for en kort beskrivelse. Her kan man bl.a. se, at CDA findes på forskellige niveauer 1-3, hvor 3 har den højeste grad af struktur.
En vigtig egenskab ved CDA dokumenter er den fælles CDA header. Denne header indeholder information, der går igen henover alle typer af kliniske dokumenter f.eks. hvilken patient drejer dokumentet sig om, hvilken organisation er ansvarlig (ejer) af dokumentet mm.
CDA headeren er således en international standard (HL7), men der findes en dansk specialisering af denne (standardiseret i regi af Medcom). Denne er beskrevet her: [HL7 Implementation Guide for CDA Release 2.0 CDA Header (DK CDA Header)](https://svn.medcom.dk/svn/releases/Standarder/HL7/CDA%20Header/Dokumentation/)

Der findes en række profiler, der er specialiseringer af CDA. Dvs. kliniske dokumenter, der har en struktur til bestemte formål. 
Medcom har leveret danske profileringer af følgende typer (se evt. [Medcoms oversigt over HL7 standarder](https://svn.medcom.dk/svn/releases/Standarder/HL7/)):
* Personal Health Monitoring Report (PHMR) til hjemmemonitorering
* Questionnaire Form Definition Document (QFDD) og Questionnaire Response Document (QRD) til patientrapporterede oplysninger (PRO)
* Appointment Document (APD) til aftaler
* Careplan (CPD)
* Questionnaire Form Definition Document (QFDD)
* Questionnaire Response Document (QRD)
* Personal Data Card (PDC)

I forhold til KIH er det typisk dokumenter af type PHMR der er relevant.
Når en hjemmemonitorering ønskes delt skal den derfor beskrives i et CDA dokument, der følger standarden beskrevet i  (HL7 Implementation Guide for CDA Release 2.0 Personal Healthcare Monitoring Report (PHMR))[https://svn.medcom.dk/svn/releases/Standarder/HL7/PHMR/Dokumentation/] med en header, der lever op til den danske profilering af CDA header.

# Hvordan deles data: XDS overblik og services
XDS står for Cross-Enterprise Document Sharing og er en international standard (IHE) for udveksling af kliniske dokumenter. Se f.eks. [IHE Cross-Enterprise Document Sharing](http://wiki.ihe.net/index.php/Cross-Enterprise_Document_Sharing).
En XDS infrastruktur består (mindst) af følgende to komponenter:
* XDS Repository: Står for persistering af dokumenter tilknyttet et unikt ID. 
* XDS Registry: Står for opbevaring og indexering af metadata vedr. dokumenterne i et eller flere XDS repositories. Dette kunne f.eks. være start- og sluttidspunkt for en måling, patienten, som målingen vedrører mm (oplysningerne stammer fra CDA headeren)
Integrationen med XDS infrastrukturen sker vha en række standardiserede SOAP webservices. Et overblik over XDS infrastrukturen og de forskellige services ses nedenfor:
![Billede af XDS Infrastruktur og ITI services burde være her](http://wiki.ihe.net/images/d/d7/XDS-Actor-Transaction-b.jpg "XDS komponenter og ITI services")

Når data skal deles vha XDS sker følgende:
1. Dokumenter afleveres af dokumentkilden (Document Source) til XDS repository via servicehåndtaget *ITI-41 Provide and Register Document Set*
2. Dokumentaftager (Document Consumer) fremsøger dokumenter i XDS registry via servicehåndtaget *ITI-18 Registry Stored Query*. Svaret på denne query er en liste af documentIds og repositoryIds, der fortæller, hvilke dokumenter der lever op til søgekriterierne, og hvor de findes (repositoryId)
3. Dokumentaftager (Document Consumer) henter dokument i XDS repositroy via servicehåndtaget *ITI-43 Retrieve Document Set*
4. Ikke på figur: Dokumentadministrator kan "deprecate" et dokument i XDS registry via servicehåndtaget *ITI-57 Update Document Set*

# Hvordan kommer man i gang med at bruge ITI håndtagene?
De udbudte services (ITI-XX) er standardiserede SOAP services. Fra et udvikler perspektiv kan man enten vælge selv at generere stubkode udfra de standardiserede WSDL filer eller at anvende et tredjepartsprodukt.
Javaudviklere kan med fordel anvende (IPF Open eHealth Integration Platform)[http://oehf.github.io/ipf/ipf-platform-camel-ihe/]. Man behøver ikke at basere alting på Camel, men kan med fordel nøjes med at inkludere biblioteket (IPF Commons IHE XDS)[https://mvnrepository.com/artifact/org.openehealth.ipf.commons/ipf-commons-ihe-xds] i sin kodebase. Her findes både stubbe og en masse anvendelige utilities.

## IPF Open eHealth Integration Platform eksempler 

Følgende eksempel på registrering af dokument (med documentId=1 for en patient med CPR-nummer 2512489996 for en afdeling med SOR kode 12345678) vha *ITI-41 Provide and Register Document Set* baserer sig på dette bibliotek:
```
ProvideAndRegisterDocumentSet provideAndRegisterDocumentSet = new ProvideAndRegisterDocumentSet();

AssigningAuthority patientIdAssigningAuthority = new AssigningAuthority("1.2.208.176.1.2"); // OID for CPR registret
Identifiable patientIdentifiable = patientIdentifiable = new Identifiable("2512489996", patientIdAssigningAuthority);

AssigningAuthority organisationAssigningAuthority = new AssigningAuthority("1.2.208.176.1"); // OID for SOR
Author author = new Author();
Organization authorOrganisation = new Organization("Afdelingen for xyz", "123456789", organisationAssigningAuthority);
author.getAuthorInstitution().add(authorOrganisation);

SubmissionSet submissionSet = new SubmissionSet();
submissionSet.setPatientId(patientIdentifiable);
submissionSet.setAuthor(author);
submissionSet.setAvailabilityStatus(AvailabilityStatus.APPROVED);


DocumentEntry documentEntry = new DocumentEntry();
documentEntry.setPatientId(patientIdentifiable);

... mere metadata (se profil)

Document document = new Document(documentEntry, new DataHandler(new ByteArrayDataSource(documentPayload.getBytes(), "text/xml")));
provideAndRegisterDocumentSet.getDocuments().add(document);
        
ProvideAndRegisterDocumentSetTransformer registerDocumentSetTransformer = new ProvideAndRegisterDocumentSetTransformer(getEbXmlFactory());
EbXMLProvideAndRegisterDocumentSetRequest30 ebxmlRequest = (EbXMLProvideAndRegisterDocumentSetRequest30) registerDocumentSetTransformer.toEbXML(provideAndRegisterDocumentSet);
ProvideAndRegisterDocumentSetRequestType provideAndRegisterDocumentSetRequestType = ebxmlRequest.getInternal();
ProvideAndRegisterDocumentSetRequest provideAndRegisterDocumentSetRequest = new ProvideAndRegisterDocumentSetRequest("1", provideAndRegisterDocumentSetRequestType);

RegistryResponseType registryResponse = iti41PortType.documentRepositoryProvideAndRegisterDocumentSetB(provideAndRegisterDocumentSetRequest.getProvideAndRegisterDocumentSetRequestType());
```

Følgende er et eksempel på fremsøgning af (godkendte) dokumenter for patient (med CPR nummer 2512489996)  vha *ITI-18 Registry Stored Query*:
```
FindDocumentsQuery fdq = new FindDocumentsQuery();
AssigningAuthority authority = new AssigningAuthority("1.2.208.176.1.2");
Identifiable patientIdentifiable = new Identifiable("2512489996", authority);
fdq.setPatientId(patientIdentifiable);	
List<AvailabilityStatus> availabilityStati = new LinkedList<>();
availabilityStati.add(AvailabilityStatus.APPROVED);
fdq.setStatus(availabilityStati);

QueryRegistry queryRegistry = new QueryRegistry(fdq);
QueryReturnType qrt = QueryReturnType.LEAF_CLASS;
if (qrt != null) {
	queryRegistry.setReturnType(qrt);
}

QueryRegistryTransformer queryRegistryTransformer = new QueryRegistryTransformer();
EbXMLAdhocQueryRequest ebxmlAdhocQueryRequest = queryRegistryTransformer.toEbXML(queryRegistry);
AdhocQueryRequest internal = (AdhocQueryRequest)ebxmlAdhocQueryRequest.getInternal();
AdhocQueryResponse adhocQueryResponse = iti18PortType.documentRegistryRegistryStoredQuery(adhocQueryRequest);
```

Følgende er et eksempel på hentning af dokument (med documentId=1) fra XDS Repository (med repositoryId=999) vha *ITI-43 Retrieve Document Set*:
```
RetrieveDocumentSetRequestType retrieveDocumentSetRequestType = new RetrieveDocumentSetRequestType();
RetrieveDocumentSetRequestType.DocumentRequest documentRequest = new RetrieveDocumentSetRequestType.DocumentRequest();
documentRequest.setRepositoryUniqueId("999");
documentRequest.setDocumentUniqueId("1");
retrieveDocumentSetRequestType.getDocumentRequest().add(documentRequest);

RetrieveDocumentSetResponseType repositoryResponse = iti43PortType.documentRepositoryRetrieveDocumentSet(retrieveDocumentSetRequestType);
```

## Komplet java eksempel

### Opsætning af udviklingsmiljø

* Kildekoden findes i github: https://github.com/KvalitetsIT/kih-anvendelse
* Kan bygges med maven og Java 8: mvn clean install 

### Beskrivelse af kodens struktur

* Hoved applikationen er Application.
* Herfra kaldes DocumentProcessor, som udfører følgende trin
  * Søgning af dokumenter
  * Registrering af nyt dokument
  * Læsning af dokument
  * Registrering af dokument som erstatning for det førest
  * Deprecate af dokument
* DokumentProccesser arbejder med et DocumentHelper objekt (ex DokumentHelperPHMRImpl), der håndtere de dokumentspecifikke ting. På den måde holdes DokumentProccesser logikken helt generel.
* DokumentHelperPHMRImpl og de andre implementeringer af DokumentHelper håndterer følgende, som er dokument type specifik
  * Opretter et dokument vha. DocumentFactory(PHMR)
  * Bygger det til et XML dokument vha. CDA builder/parseren
  * Kan oplyse om de dokument type specfikke metadata
  * Kan returnere oprettelses tidspunktet for dokumentet
  * Kan opgive dokumentets type kode
* CdaMetaDataFactory skaber metadata vha. CDA builder/parseren med udgangspunkt i XML dokumentet
* XdsRequestBuilderService bygger iti kaldene
* XdsRequestService udfører iti kalene
* Konfiguration af endspoints gøres i property filerne application.properties og dgws.properties


Hvis en ny dokumenttype skal introduceres i eksemplet, oprettes
* DocumentHelper<dokumenttype>Impl samt
* DocumentFactory<dokumenttype>
* Kaldet aktiveres i Application


### Aktivering af kald

* For at udføres faktiske iti kald mod en server køres klassen "Application"
* De forskellige dokument typer kan slåes fra og til ved at ændre på boolean i if-sætningen foran hver type
* Kaldene udføres mod de endpoints, som er angivet i application.properties (ITI) og dgws.properties (STS) 

## Endpoints

I java eksemplet er "kih test" server sat op (den første i listen nedenfor). Der er dog en række andre kombinationer, er kan bruges hvis andre registry og repositories ønskes.


| Server         | Type               | Endpoint/Værdi                                                               | NB |
|----------------|--------------------|------------------------------------------------------------------------------|----| 
| KIH test       | repositoryuniqueid | 1.2.208.176.43210.8.1.29                                                     |    |
|                | iti18.endpoint     | http://test2-cnsp.ekstern-test.nspop.dk:8080/ddsregistry                     |    |
|                | iti57.endpoint     | https://test2-cnsp.ekstern-test.nspop.dk:8443/dros/iti57                     |    |
|                | iti43.endpoint     | http://test2-cnsp.ekstern-test.nspop.dk:8080/ddsrepository                   |    |
|                | iti41.endpoint     | https://kih.test.xdsrepositoryb.medcom.dk/kih-iti41/iti41  	             |    |
|                | sts.url (dgws)     | http://test2.ekstern-test.nspop.dk:8080/sts/services/NewSecurityTokenService |    |
|                | cda viewer         | https://cdaviewer.medcom.dk/cdaviewer-test2/                                 | *1 |
| KIH uddannelse | repositoryuniqueid | 1.2.208.176.43210.8.1.31                                                     |    |
|                | iti18.endpoint     | http://test2-cnsp.ekstern-test.nspop.dk:8080/ddsregistry                     |    |
|                | iti57.endpoint     | https://test2-cnsp.ekstern-test.nspop.dk:8443/dros/iti57                     |    |
|                | iti43.endpoint     | http://test2-cnsp.ekstern-test.nspop.dk:8080/ddsrepository                   |    |
|                | iti41.endpoint     | http://kihrepository-sec-udd-npi-nsi.rn.dsdn.dk:8022/kih-iti41/iti41         | *2 |
|                | sts.url (dgws)     | http://test2.ekstern-test.nspop.dk:8080/sts/services/NewSecurityTokenService |    |
|                | cda viewer         | https://cdaviewer.medcom.dk/cdaviewer-test2/                                 | *1 |
| Test 1         | repositoryuniqueid | 1.2.208.176.43210.8.10.11                                                    |    |
|                | iti18.endpoint     | http://test1-cnsp.ekstern-test.nspop.dk:8080/ddsregistry                     |    |
|                | iti57.endpoint     | https://test1-cnsp.ekstern-test.nspop.dk:8443/dros/iti57                     |    |
|                | iti43.endpoint     | http://test1-cnsp.ekstern-test.nspop.dk:8080/ddsrepository                   |    |
|                | iti41.endpoint     | https://test1-cnsp.ekstern-test.nspop.dk:8443/dros/iti41                     |    |
|                | sts.url (dgws)     | http://test1.ekstern-test.nspop.dk:8080/sts/services/NewSecurityTokenService |    |
|                | cda viewer         | https://cdaviewer.medcom.dk/cdaviewer-test1/                                 | *1 |
| Test 2         | repositoryuniqueid | 1.2.208.176.43210.8.20.11 	                                             |    |
|                | iti18.endpoint     | http://test2-cnsp.ekstern-test.nspop.dk:8080/ddsregistry                     |    |
|                | iti57.endpoint     | https://test2-cnsp.ekstern-test.nspop.dk:8443/dros/iti57                     |    |
|                | iti43.endpoint     | http://test2-cnsp.ekstern-test.nspop.dk:8080/ddsrepository                   |    |
|                | iti41.endpoint     | https://test2-cnsp.ekstern-test.nspop.dk:8443/dros/iti41     	             |    |
|                | sts.url (dgws)     | http://test2.ekstern-test.nspop.dk:8080/sts/services/NewSecurityTokenService |    |
|                | cda viewer         | https://cdaviewer.medcom.dk/cdaviewer-test2/                                 | *1 |
| KIH prod       | repositoryuniqueid | 1.2.208.176.8.1.30                                                           |    |
|                | iti18.endpoint     | kontakt Medcom/SDS                                                           |    |
|                | iti57.endpoint     | kontakt Medcom/SDS                                                           |    |
|                | iti43.endpoint     | kontakt Medcom/SDS                                                           |    |
|                | iti41.endpoint     | https://kihrepository-sec-npi-nsi.rn.dsdn.dk:8022/kih-iti41/iti41            | *2 |
|                | sts.url (dgws)     | kontakt Medcom/SDS                                                           |    |

*1: kræver login
*2: kræver SDN aftale 

 

## Hjælpeværktøjer

Medcom har følgende hjælpeværktøjer, som stilles til rådighed i forbindelse med udvikling og test.

* [CDA viewer](https://cdaviewer.medcom.dk/cdaviewer/).
  * Her kan man fremsøge dokumenter, man har registreret
  * Kræver et login, som fåes hos Medcom
  * Der findes en viewer til test1 og en til test2. Sidstnævnte anvendes også til KIH test
* [CDA validator](http://cda.medcom.dk/#/view_direct_input)
  * Her kan man validere de forskellige typer af CDA dokumenter
* [Medcoms komme godt igang guide](https://www.medcom.dk/media/10982/kom-godt-igang-med-dokumentdeling-14-interactive.pdf)
