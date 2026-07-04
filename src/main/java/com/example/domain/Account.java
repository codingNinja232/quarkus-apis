package com.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(name = "parentid")
    public Integer parentId;

    @Column(name = "rootaccountid")
    public Integer rootAccountId;

    public String name;
    public String type;

    @Column(name = "contactid")
    public Integer contactId;

    @Column(name = "contractid")
    public Integer contractId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "parentid", insertable = false, updatable = false)
    public Account parentAccount;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "rootaccountid", insertable = false, updatable = false)
    public Account rootAccount;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "contactid", insertable = false, updatable = false)
    public Contact contact;
}
