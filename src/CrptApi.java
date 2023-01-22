import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CrptApi {

    /*Основные данные для работы с запросом*/
    private final String URL = "http://<server-name>[:server-port]" +
            "/api/v2/{extension}/ rollout?omsId={omsId}";
    private final String CLIENT_TOKEN = "clientToken";
    private final String USER_NAME = "userName";
    private int requestLimit;
    private final TimeUnit timeUnit;
    private static int counter; // используем переменную для счета количества запросов


    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        if (requestLimit >= 0) {
            this.requestLimit = requestLimit;
            counter = requestLimit;
        } else {
            throw new IllegalArgumentException("Передано отрицательное число");
        }
    }

    /*Метод для запуска запроса*/
    public void runRequest(Document document, String signature) {
        String docJson = getDocJson(document, signature).toString();
        httpRequest(docJson);
    }

    /*Создание Json объекта, по переданному для запроса, Java документу*/
    private JSONObject getDocJson(Document document, String signature) {
        JSONObject doc = new JSONObject();
        if (isNull(document.getDescription())) {
            JSONObject inn = new JSONObject();
            inn.put("participantInn", document.getParticipantInn());
            doc.put("description", inn);
        }
        doc.put("doc_id", document.getDocId());
        doc.put("doc_status", document.getDocStatus());
        doc.put("doc_type", document.getDocType());
        if (isNull(document.getImportRequest())) {
            doc.put("importRequest", document.getImportRequest());
        }
        doc.put("owner_inn", document.getOwnerInn());
        doc.put("participant_inn", document.getParticipantInn());
        doc.put("producer_inn", document.getProducerInn());
        doc.put("production_date", document.getProducerInn());
        doc.put("production_type", document.getProductionType());
        Document.Products product = document.getProducts();
        if (product != null) {
            JSONArray productsList = new JSONArray();
            JSONObject products = new JSONObject();
            if (product.getCertificateDocument() != null) {
                products.put("certificate_document", product.getCertificateDocument());
            } else if (isNull(product.getCertificateDocumentDate())) {
                products.put("certificate_document_date", product.getCertificateDocumentDate());
            } else if (isNull(product.getCertificateDocumentNumber())) {
                products.put("certificate_document_number", product.getCertificateDocumentNumber());
            }
            products.put("owner_inn", document.getOwnerInn());
            products.put("producer_inn", document.getProducerInn());
            products.put("production_date", document.getProductionDate());
            if (!document.getProductionDate().equals(product.getProductionDate())) {
                products.put("production_date", product.getProductionDate());
            }
            products.put("tnved_code", product.tnvedCode);
            if (isNull(product.getUitCode())) {
                products.put("uit_code", product.getUitCode());
            } else if (isNull(product.getUituCode())) {
                products.put("uitu_code", product.getUituCode());
            } else {
                throw new IllegalArgumentException("Одно из полей uit_code/uitu_code " +
                        "является обязательным");
            }
            productsList.add(products);
            doc.put("products", productsList);
        }
        doc.put("reg_date", document.getRegDate());
        doc.put("reg_number", document.getRegNumber());
        doc.put("signature", signature);
        return doc;
    }

    /*Метод для подготовки и создания HTTP запроса*/
    private void httpRequest(String json) {
        if (requestLimit != 0) {
            synchronized (this) {
                counter--;
            }
        }
        try {
            if (counter < 0) {
                Thread.sleep(getTime());
                counter = requestLimit;
            }
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(URL);

            StringEntity entity = new StringEntity(json);
            post.addHeader("content-type", "application/json");
            post.addHeader("clientToken", CLIENT_TOKEN);
            post.addHeader("userName", USER_NAME);
            post.setEntity(entity);
            httpClient.execute(post);
            httpClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isNull(String check) {
        return check != null;
    }

    public enum TimeUnit {
        SECOND, MINUTE, HOUR
    }

    private long getTime() {
        return switch (timeUnit) {
            case SECOND -> 1000;
            case MINUTE -> 1000 * 60;
            case HOUR -> 1000 * 60 * 60;
        };
    }

    /*Класс для полей, которые есть в документе, для создания Json объекта*/

    public static class Document {
        private String description;
        private final String participantInn;
        private final String docId;
        private final String docStatus;
        private final String docType;
        private String importRequest;
        private final String ownerInn;
        private final String producerInn;
        private final String productionDate;
        private final String productionType;
        private final String regDate;
        private final String regNumber;
        private Products products;

        //Геттеры и сеттеры

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public String getDocId() {
            return docId;
        }

        public String getDocStatus() {
            return docStatus;
        }

        public String getDocType() {
            return docType;
        }

        public String getImportRequest() {
            return importRequest;
        }

        public void setImportRequest(String importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public String getProductionType() {
            return productionType;
        }

        public String getRegDate() {
            return regDate;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public Products getProducts() {
            return products;
        }

        public void setProducts(Products products) {
            this.products = products;
        }

        public Document(String participantInn, String docId, String docStatus,
                        String docType, String ownerInn, String producerInn,
                        String productionDate, String productionType,
                        String regDate, String regNumber) {
            this.participantInn = participantInn;
            this.docId = docId;
            this.docStatus = docStatus;
            this.docType = docType;
            this.ownerInn = ownerInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.productionType = productionType;
            this.regDate = regDate;
            this.regNumber = regNumber;
        }

        public class Products {
            private CertificateType certificateDocument;
            private String certificateDocumentDate;
            private String certificateDocumentNumber;
            private String productionDate;
            private String tnvedCode;
            private String uitCode;
            private String uituCode;
            //Геттеры и сеттеры
            public CertificateType getCertificateDocument() {
                return certificateDocument;
            }

            public void setCertificateDocument(CertificateType certificateDocument) {
                this.certificateDocument = certificateDocument;
            }

            public String getCertificateDocumentDate() {
                return certificateDocumentDate;
            }

            public void setCertificateDocumentDate(String certificateDocumentDate) {
                this.certificateDocumentDate = certificateDocumentDate;
            }

            public String getCertificateDocumentNumber() {
                return certificateDocumentNumber;
            }

            public void setCertificateDocumentNumber(String certificateDocumentNumber) {
                this.certificateDocumentNumber = certificateDocumentNumber;
            }

            public String getProductionDate() {
                return productionDate;
            }

            public void setProductionDate(String productionDate) {
                this.productionDate = productionDate;
            }

            public String getTnvedCode() {
                return tnvedCode;
            }

            public void setTnvedCode(String tnvedCode) {
                this.tnvedCode = tnvedCode;
            }

            public String getUitCode() {
                return uitCode;
            }

            public void setUitCode(String uitCode) {
                this.uitCode = uitCode;
            }

            public String getUituCode() {
                return uituCode;
            }

            public void setUituCode(String uituCode) {
                this.uituCode = uituCode;
            }

            public enum CertificateType {
                CONFORMITY_CERTIFICATE, CONFORMITY_DECLARATION
            }
        }
    }
}