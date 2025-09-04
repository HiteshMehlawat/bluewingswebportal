package com.adspeek.authservice.config;

import com.adspeek.authservice.entity.ServiceCategory;
import com.adspeek.authservice.entity.ServiceSubcategory;
import com.adspeek.authservice.entity.ServiceItem;
import com.adspeek.authservice.repository.ServiceCategoryRepository;
import com.adspeek.authservice.repository.ServiceSubcategoryRepository;
import com.adspeek.authservice.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceDataInitializer implements CommandLineRunner {

    private final ServiceCategoryRepository categoryRepository;
    private final ServiceSubcategoryRepository subcategoryRepository;
    private final ServiceItemRepository itemRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no categories exist
        if (categoryRepository.count() == 0) {
            log.info("Initializing service catalog data...");
            initializeServiceCatalog();
            log.info("Service catalog data initialized successfully!");
        } else {
            log.info("Service catalog data already exists, skipping initialization.");
        }
    }

    private void initializeServiceCatalog() {
        // 1. TAX PREPARATION AND FILING SERVICES
        ServiceCategory taxPreparation = createCategory("TAX PREPARATION AND FILING SERVICES",
                "Comprehensive tax preparation and filing services for individuals and businesses");

        ServiceSubcategory individualTaxes = createSubcategory(taxPreparation, "INDIVIDUAL TAXES",
                "Tax services for individual taxpayers");
        ServiceSubcategory businessTaxes = createSubcategory(taxPreparation, "BUSINESS TAXES",
                "Tax services for business entities");
        ServiceSubcategory internationalTaxes = createSubcategory(taxPreparation, "INTERNATIONAL TAXES",
                "International tax compliance and filing services");

        // Individual Taxes Items
        createServiceItem(individualTaxes, "Residents (1040)", "Individual tax return for US residents", 2);
        createServiceItem(individualTaxes, "Nonresidents (1040NR)", "Individual tax return for non-US residents", 3);
        createServiceItem(individualTaxes, "Immigrants (F1, J1, O1, H1B, EAD)",
                "Tax services for immigrants and visa holders", 4);
        createServiceItem(individualTaxes, "Salary Income", "Tax preparation for salary and wage income", 1);
        createServiceItem(individualTaxes, "Contractor Income and Expenses",
                "Tax preparation for contractor income and business expenses", 2);

        // Business Taxes Items
        createServiceItem(businessTaxes, "Partnership (1065)", "Partnership tax return preparation", 3);
        createServiceItem(businessTaxes, "Limited Liability Company (LLC)", "LLC tax return preparation", 2);
        createServiceItem(businessTaxes, "C-Corporation (1120)", "C-Corporation tax return preparation", 4);
        createServiceItem(businessTaxes, "S-Corporation (1120S)", "S-Corporation tax return preparation", 3);
        createServiceItem(businessTaxes, "Sole Proprietorship (Sch C)", "Sole proprietorship tax return preparation",
                2);

        // International Taxes Items
        createServiceItem(internationalTaxes, "Foreign Tax Credits (1116)", "Foreign tax credit calculation and filing",
                3);
        createServiceItem(internationalTaxes, "Foreign Owned US Entities (1120, 5472)",
                "Tax returns for foreign-owned US entities", 5);
        createServiceItem(internationalTaxes, "Foreign Corporations (1120-F, 5471)",
                "Foreign corporation tax compliance", 4);
        createServiceItem(internationalTaxes, "Foreign Partnerships (8865)",
                "Foreign partnership tax return preparation", 4);
        createServiceItem(internationalTaxes, "Foreign Branches (8858)", "Foreign branch tax return preparation", 3);

        // 2. ITIN SERVICES
        ServiceCategory itinServices = createCategory("ITIN SERVICES",
                "Individual Taxpayer Identification Number application and renewal services");

        ServiceSubcategory w7ItinApplication = createSubcategory(itinServices, "W7 & ITIN Application",
                "W7 ITIN application filing services");
        ServiceSubcategory certifiedAcceptanceAgent = createSubcategory(itinServices, "Certified Acceptance Agent",
                "Certified Acceptance Agent services for ITIN applications");

        // W7 & ITIN Application Items
        createServiceItem(w7ItinApplication, "W7 ITIN Application Filing", "Complete W7 ITIN application filing", 2);
        createServiceItem(w7ItinApplication, "ITIN for Nonresident Spouse and Dependents",
                "ITIN application for nonresident family members", 2);
        createServiceItem(w7ItinApplication, "ITIN for Nonresident US Business Owners (LLC/C-Corps)",
                "ITIN for nonresident business owners", 3);
        createServiceItem(w7ItinApplication, "ITIN Renewal Application", "ITIN renewal application filing", 1);
        createServiceItem(w7ItinApplication, "1040 and 1040NR Tax Filing", "Tax filing with ITIN application", 3);

        // Certified Acceptance Agent Items
        createServiceItem(certifiedAcceptanceAgent, "CAA Authorized by Internal Revenue Service (IRS)",
                "IRS authorized CAA services", 1);
        createServiceItem(certifiedAcceptanceAgent, "Passport verification",
                "Passport verification for ITIN applications", 1);
        createServiceItem(certifiedAcceptanceAgent, "Documentation certification",
                "Document certification for ITIN applications", 1);
        createServiceItem(certifiedAcceptanceAgent, "Issue Certificate of Accuracy to IRS",
                "Certificate of accuracy issuance", 1);
        createServiceItem(certifiedAcceptanceAgent, "Coordinate with IRS ITIN Operations office",
                "Coordination with IRS ITIN operations", 2);

        // 3. TAX ADVISORY
        ServiceCategory taxAdvisory = createCategory("TAX ADVISORY",
                "Comprehensive tax advisory and planning services");

        ServiceSubcategory personalTaxFiling = createSubcategory(taxAdvisory, "PERSONAL TAX FILING",
                "Personal tax filing advisory services");
        ServiceSubcategory businessTaxPlanning = createSubcategory(taxAdvisory, "BUSINESS TAX PLANNING",
                "Business tax planning and advisory services");
        ServiceSubcategory generalAdvisory = createSubcategory(taxAdvisory, "GENERAL ADVISORY",
                "General tax advisory and consultation services");

        // Personal Tax Filing Items
        createServiceItem(personalTaxFiling, "Personal Financial Goals Identification",
                "Identify personal financial goals for tax planning", 1);
        createServiceItem(personalTaxFiling, "Tax Return Review", "Comprehensive tax return review", 2);
        createServiceItem(personalTaxFiling, "Identify Missed Deductions and Credits",
                "Identify missed tax deductions and credits", 2);
        createServiceItem(personalTaxFiling, "Cost Benefit Analysis for Amendments",
                "Cost-benefit analysis for tax amendments", 1);
        createServiceItem(personalTaxFiling, "Employer Stock Option Advice", "Advice on employer stock options", 2);

        // Business Tax Planning Items
        createServiceItem(businessTaxPlanning, "Business Financial Goals Identification",
                "Identify business financial goals", 1);
        createServiceItem(businessTaxPlanning, "Tax Return Review & Projections",
                "Tax return review and future projections", 3);
        createServiceItem(businessTaxPlanning, "Retirement Account Planning (401k, SEP, SIMPLE IRA)",
                "Retirement account planning and setup", 2);
        createServiceItem(businessTaxPlanning, "Payroll Planning for Owners and Family",
                "Payroll planning for business owners and family", 2);
        createServiceItem(businessTaxPlanning, "Entity Restructuring (LLC v/s C-Corp v/s S-Corp)",
                "Business entity restructuring advice", 3);

        // General Advisory Items
        createServiceItem(generalAdvisory, "15 Min Free Introductory Call (No Tax Advice Given Here)",
                "Free introductory consultation call", 0);
        createServiceItem(generalAdvisory, "30 Min EA/CPA Tax Consultation", "30-minute tax consultation with EA/CPA",
                1);
        createServiceItem(generalAdvisory, "60 Min EA/CPA Tax Consultation", "60-minute comprehensive tax consultation",
                1);
        createServiceItem(generalAdvisory, "Secretary of State Renewals", "Secretary of state renewal services", 1);
        createServiceItem(generalAdvisory, "All Federal and State Compliances",
                "Comprehensive federal and state compliance services", 4);

        // 4. BUSINESS SERVICES
        ServiceCategory businessServices = createCategory("BUSINESS SERVICES",
                "Comprehensive business services including bookkeeping, payroll, and virtual CFO");

        ServiceSubcategory bookkeeping = createSubcategory(businessServices, "BOOKKEEPING",
                "Professional bookkeeping services");
        ServiceSubcategory payroll = createSubcategory(businessServices, "PAYROLL",
                "Payroll processing and management services");
        ServiceSubcategory virtualCFO = createSubcategory(businessServices, "VIRTUAL CFO",
                "Virtual Chief Financial Officer services");

        // Bookkeeping Items
        createServiceItem(bookkeeping, "Categorize Transactions in QuickBooks",
                "Transaction categorization in QuickBooks", 2);
        createServiceItem(bookkeeping, "Prepare Profit and Loss Statement and Balance Sheet",
                "Financial statement preparation", 3);
        createServiceItem(bookkeeping, "Prepare Financial Statements", "Comprehensive financial statement preparation",
                4);
        createServiceItem(bookkeeping, "Bank Reconciliations", "Monthly bank reconciliation services", 2);
        createServiceItem(bookkeeping, "Estimated Quarterly Advance Tax Calculations",
                "Quarterly tax estimation and calculations", 2);

        // Payroll Items
        createServiceItem(payroll, "Payroll Setup", "Complete payroll system setup", 3);
        createServiceItem(payroll, "Automate Direct Deposit", "Direct deposit automation setup", 1);
        createServiceItem(payroll, "Withholding IRS and State Taxes", "Tax withholding management", 2);
        createServiceItem(payroll, "Filing Payroll Reports", "Payroll tax report filing", 2);
        createServiceItem(payroll, "Apply for SUI ID", "State Unemployment Insurance ID application", 1);

        // Virtual CFO Items
        createServiceItem(virtualCFO, "All Bookkeeping Services", "Comprehensive bookkeeping services", 8);
        createServiceItem(virtualCFO, "All Payroll Services", "Complete payroll management services", 6);
        createServiceItem(virtualCFO, "Tax Advisory and Planning", "Comprehensive tax advisory services", 4);
        createServiceItem(virtualCFO, "Expense Planning and Budgeting", "Expense planning and budget management", 3);
        createServiceItem(virtualCFO, "Retirement Account Planning And Setup",
                "Retirement account planning and implementation", 3);

        // 5. COMPANY FORMATION
        ServiceCategory companyFormation = createCategory("COMPANY FORMATION",
                "Business formation and compliance services");

        ServiceSubcategory llcCorpFormation = createSubcategory(companyFormation, "LLC, C-CORP, S-CORP FORMATION",
                "Business entity formation services");
        ServiceSubcategory registeredAgent = createSubcategory(companyFormation,
                "REGISTERED AGENT AND VIRTUAL BUSINESS ADDRESS",
                "Registered agent and virtual address services");
        ServiceSubcategory ctaBoiFiling = createSubcategory(companyFormation, "CTA BOI AND BE 12/13 FILLING",
                "Corporate Transparency Act and BOI filing services");

        // LLC, C-CORP, S-CORP FORMATION Items
        createServiceItem(llcCorpFormation, "For US Residents and Foreigners (US Nonresidents)",
                "Business formation for residents and nonresidents", 3);
        createServiceItem(llcCorpFormation, "SS4 and Federal Tax EIN Application", "EIN application and SS4 filing", 2);
        createServiceItem(llcCorpFormation, "Business Structure Analysis - LLC v/s C-Corp v/s S-Corp",
                "Business structure analysis and recommendations", 2);
        createServiceItem(llcCorpFormation, "Name Availability Search", "Business name availability search", 1);
        createServiceItem(llcCorpFormation, "Secretary of State Filings and Fees Submissions",
                "State filing and fee submission services", 2);

        // Registered Agent Items
        createServiceItem(registeredAgent, "Registered Agent Services in almost all states",
                "Registered agent services nationwide", 1);
        createServiceItem(registeredAgent, "Discounted Wyoming & Delaware services",
                "Discounted services for Wyoming and Delaware", 1);
        createServiceItem(registeredAgent, "Dedicate Business Address with Suite Numbers",
                "Dedicated business address with suite numbers", 1);
        createServiceItem(registeredAgent, "No PO Box. Real Physical Addresses", "Real physical business addresses", 1);
        createServiceItem(registeredAgent, "Dedicated Online Account & Dashboard",
                "Online account and dashboard access", 1);

        // CTA BOI Items
        createServiceItem(ctaBoiFiling, "Compliance under Corporate Transparency Act",
                "Corporate Transparency Act compliance", 3);
        createServiceItem(ctaBoiFiling, "Customized Beneficial Ownership Information (BOI) Report",
                "Customized BOI report preparation", 2);
        createServiceItem(ctaBoiFiling, "Submission Confirmation from FinCen", "FinCen submission confirmation", 1);
        createServiceItem(ctaBoiFiling, "Ongoing BOI Report Update Filings", "Ongoing BOI report update services", 2);
        createServiceItem(ctaBoiFiling, "U.S. Bureau of Economic Analysis (BEA) Reporting", "BEA reporting services",
                3);
    }

    private ServiceCategory createCategory(String name, String description) {
        ServiceCategory category = ServiceCategory.builder()
                .name(name)
                .description(description)
                .isActive(true)
                .build();
        return categoryRepository.save(category);
    }

    private ServiceSubcategory createSubcategory(ServiceCategory category, String name, String description) {
        ServiceSubcategory subcategory = ServiceSubcategory.builder()
                .name(name)
                .description(description)
                .category(category)
                .isActive(true)
                .build();
        return subcategoryRepository.save(subcategory);
    }

    private ServiceItem createServiceItem(ServiceSubcategory subcategory, String name, String description,
            Integer estimatedHours) {
        ServiceItem item = ServiceItem.builder()
                .name(name)
                .description(description)
                .estimatedHours(estimatedHours)
                .subcategory(subcategory)
                .isActive(true)
                .build();
        return itemRepository.save(item);
    }
}
