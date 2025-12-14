package com.ebuy.product.search.repository;

import com.ebuy.product.search.entity.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.opensearch.client.opensearch.core.*;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ProductSearchRepository {

    private static final String INDEX_NAME = "products";

    @Autowired
    private OpenSearchClient openSearchClient;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void initializeIndex() {
        try {
            createIndexIfNotExists();
        } catch (Exception e) {
            log.error("Failed to initialize product index", e);
        }
    }

    private void createIndexIfNotExists() throws IOException {
        ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(INDEX_NAME));

        if (!openSearchClient.indices().exists(existsRequest).value()) {
            log.info("Creating index: {}", INDEX_NAME);

            CreateIndexRequest createIndexRequest = CreateIndexRequest.of(c -> c
                    .index(INDEX_NAME)
                    .mappings(m -> m
                            .properties("product_id", p -> p.long_(l -> l))
                            .properties("name", p -> p.text(t -> t.analyzer("standard")))
                            .properties("description", p -> p.text(t -> t.analyzer("standard")))
                            .properties("price", p -> p.double_(d -> d))
                            .properties("discounted_price", p -> p.double_(d -> d))
                            .properties("brand", p -> p.keyword(k -> k))
                            .properties("tags", p -> p.keyword(k -> k))
                            .properties("search_keywords", p -> p.text(t -> t.analyzer("standard")))
                            .properties("is_active", p -> p.boolean_(b -> b))
                            .properties("is_in_stock", p -> p.boolean_(b -> b))
                            .properties("popularity_score", p -> p.float_(f -> f))
                            .properties("search_boost", p -> p.float_(f -> f))
                            .properties("created_at", p -> p.date(d -> d))
                            .properties("modified_at", p -> p.date(d -> d))
                    )
            );

            openSearchClient.indices().create(createIndexRequest);
            log.info("Index created successfully: {}", INDEX_NAME);
        } else {
            log.info("Index already exists: {}", INDEX_NAME);
        }
    }

    public void indexProduct(Product product) throws IOException {
        log.debug("Indexing product: {}", product.getProductId());

        IndexRequest<Product> request = IndexRequest.of(i -> i
                .index(INDEX_NAME)
                .id(String.valueOf(product.getProductId()))
                .document(product)
        );

        IndexResponse response = openSearchClient.index(request);
        log.debug("Product indexed with result: {}", response.result());
    }

    public void indexProducts(List<Product> products) throws IOException {
        log.info("Bulk indexing {} products", products.size());

        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();

        for (Product product : products) {
            bulkBuilder.operations(op -> op
                    .index(idx -> idx
                            .index(INDEX_NAME)
                            .id(String.valueOf(product.getProductId()))
                            .document(product)
                    )
            );
        }

        BulkResponse response = openSearchClient.bulk(bulkBuilder.build());

        if (response.errors()) {
            log.error("Bulk indexing had errors");
            response.items().forEach(item -> {
                if (item.error() != null) {
                    log.error("Error indexing item {}: {}", item.id(), item.error().reason());
                }
            });
        } else {
            log.info("Successfully bulk indexed {} products", products.size());
        }
    }

    public Optional<Product> findById(Long productId) throws IOException {
        log.debug("Finding product by ID: {}", productId);

        try {
            GetRequest getRequest = GetRequest.of(g -> g
                    .index(INDEX_NAME)
                    .id(String.valueOf(productId))
            );

            GetResponse<Product> response = openSearchClient.get(getRequest, Product.class);

            if (response.found()) {
                return Optional.of(response.source());
            }
        } catch (Exception e) {
            log.error("Error finding product by ID: {}", productId, e);
        }

        return Optional.empty();
    }

    public List<Product> searchProducts(String query, int from, int size) throws IOException {
        log.debug("Searching products with query: '{}', from: {}, size: {}", query, from, size);

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .query(buildSearchQuery(query))
                .from(from)
                .size(size)
                .sort(so -> so
                        .field(f -> f
                                .field("_score")
                                .order(SortOrder.Desc)
                        )
                )
                .sort(so -> so
                        .field(f -> f
                                .field("popularity_score")
                                .order(SortOrder.Desc)
                        )
                )
        );

        SearchResponse<Product> response = openSearchClient.search(searchRequest, Product.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    public List<Product> searchByCategory(String categoryName, int from, int size) throws IOException {
        log.debug("Searching products by category: '{}', from: {}, size: {}", categoryName, from, size);

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .query(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .term(t -> t
                                                .field("category.name.keyword")
                                                .value(FieldValue.of(categoryName))
                                        )
                                )
                                .must(m -> m
                                        .term(t -> t
                                                .field("is_active")
                                                .value(FieldValue.of(true))
                                        )
                                )
                        )
                )
                .from(from)
                .size(size)
                .sort(so -> so
                        .field(f -> f
                                .field("popularity_score")
                                .order(SortOrder.Desc)
                        )
                )
        );

        SearchResponse<Product> response = openSearchClient.search(searchRequest, Product.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    public List<String> getAutocompleteSuggestions(int categoryId, String prefix, int size) throws IOException {
        log.debug("Getting autocomplete suggestions for categoryId: {}, prefix: '{}', size: {}", categoryId, prefix, size);

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .query(q -> q
                        .bool(b -> {
                            // Add the should clauses for prefix matching
                            b.should(sh -> sh
                                            .prefix(p -> p
                                                    .field("name")
                                                    .value(prefix)
                                            )
                                    )
                                    .should(sh -> sh
                                            .prefix(p -> p
                                                    .field("search_keywords")
                                                    .value(prefix)
                                            )
                                    );

                            // Always filter by active products
                            b.must(m -> m
                                    .term(t -> t
                                            .field("is_active")
                                            .value(FieldValue.of(true))
                                    )
                            );

                            // Add category filtering only if categoryId is not 0
                            if (categoryId != 0) {
                                b.must(m -> m
                                        .term(t -> t
                                                .field("category.category_id")
                                                .value(FieldValue.of(categoryId))
                                        )
                                );
                            }

                            return b;
                        })
                )
                .size(size)
                .source(so -> so
                        .filter(f -> f
                                .includes("name", "search_keywords")
                        )
                )
        );

        SearchResponse<Product> response = openSearchClient.search(searchRequest, Product.class);

        return response.hits().hits().stream()
                .map(hit -> hit.source().getName())
                .distinct()
                .collect(Collectors.toList());
    }

    public void deleteProduct(Long productId) throws IOException {
        log.debug("Deleting product: {}", productId);

        DeleteRequest deleteRequest = DeleteRequest.of(d -> d
                .index(INDEX_NAME)
                .id(String.valueOf(productId))
        );

        DeleteResponse response = openSearchClient.delete(deleteRequest);
        log.debug("Product deleted with result: {}", response.result());
    }

    public void updateProduct(Product product) throws IOException {
        log.debug("Updating product: {}", product.getProductId());

        UpdateRequest<Product, Product> updateRequest = UpdateRequest.of(u -> u
                .index(INDEX_NAME)
                .id(String.valueOf(product.getProductId()))
                .doc(product)
                .docAsUpsert(true)
        );

        UpdateResponse<Product> response = openSearchClient.update(updateRequest, Product.class);
        log.debug("Product updated with result: {}", response.result());
    }

    private Query buildSearchQuery(String queryString) {
        if (queryString == null || queryString.trim().isEmpty()) {
            return MatchAllQuery.of(m -> m)._toQuery();
        }

        return BoolQuery.of(b -> b
                .must(m -> m
                        .term(t -> t
                                .field("is_active")
                                .value(FieldValue.of(true))
                        )
                )
                .must(m -> m
                        .term(t -> t
                                .field("is_in_stock")
                                .value(FieldValue.of(true))
                        )
                )
                .should(sh -> sh
                        .multiMatch(mm -> mm
                                .query(queryString)
                                .fields("name^3", "description^2", "search_keywords^2", "brand^1.5")
                                .fuzziness("AUTO")
                        )
                )
                .should(sh -> sh
                        .wildcard(w -> w
                                .field("name.keyword")
                                .value("*" + queryString.toLowerCase() + "*")
                        )
                )
                .minimumShouldMatch("1")
        )._toQuery();
    }

    public long getTotalProductCount() throws IOException {
        CountRequest countRequest = CountRequest.of(c -> c
                .index(INDEX_NAME)
                .query(q -> q
                        .term(t -> t
                                .field("is_active")
                                .value(FieldValue.of(true))
                        )
                )
        );

        CountResponse response = openSearchClient.count(countRequest);
        return response.count();
    }
}