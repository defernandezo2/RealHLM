package com.example.RealHLM.repositories;

import com.example.RealHLM.entities.WebPage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class SearchRepositoryImp implements SearchRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    @Override
    public WebPage getByUrl(String url) {
        String query = "FROM WebPage WHERE url= :url";
        List<WebPage> webPageList= entityManager.createQuery(query)
                .setParameter("url", url)
                .getResultList();
        return webPageList.size() ==0 ? null: webPageList.get(0);
    }

    @Override
    public List<WebPage> getLinksToIndex() {
        String query = "FROM WebPage WHERE title is null AND description is null";
        return entityManager.createQuery(query).setMaxResults(100).getResultList();    }

    @Transactional
    @Override
    public List<WebPage> search(String textSearch) {
        String query = "FROM WebPage WHERE description like :textSearch";
        return entityManager.createQuery(query).setParameter("textSearch", "%" + textSearch + "%").getResultList();
    }

    @Transactional
    @Override
    public void save(WebPage webPage) {
        entityManager.merge(webPage);
    }

    @Override
    public boolean exist(String link) {
        return getByUrl(link)!=null ;
    }
}
