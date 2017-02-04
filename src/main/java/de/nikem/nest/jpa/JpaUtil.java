package de.nikem.nest.jpa;

import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import de.nikem.nest.jdbc.NikemJdbcException;

public class JpaUtil {
	public static class EntityManagerInfo {
		private final EntityManager em;
		private boolean transactionActive = false;
		public EntityManagerInfo(EntityManager em) {
			this.em = em;
		}
		public EntityManager getEm() {
			return em;
		}
		public boolean isTransactionActive() {
			return transactionActive;
		}
		public void setTransactionActive(boolean transactionActive) {
			this.transactionActive = transactionActive;
		}
	}

	public interface Work<T, R> {

		/**
		 * Applies this function to the given argument.
		 *
		 * @param t the function argument
		 * @return the function result
		 */
		R apply(T t) throws Exception;
	}

	private static class TransactionWork<T> implements Work<EntityManager, T> {
		private final Work<EntityManager, T> delegate;

		public TransactionWork(Work<EntityManager, T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public T apply(EntityManager con) throws SQLException {
			EntityManagerInfo info = threadConnection.get();
			boolean myTransaction = false;
			T result;
			try {

				if (!info.isTransactionActive()) {
					con.getTransaction().begin();
					info.setTransactionActive(true);
					myTransaction = true;
				}

				result = delegate.apply(con);

				if (myTransaction) {
					log.fine("Commit Connection for Thread " + Thread.currentThread().getName());
					con.getTransaction().commit();
				}
			} catch (RuntimeException e) {
				if (myTransaction) {
					log.fine("Rollback Connection for Thread " + Thread.currentThread().getName());
					rollback(con.getTransaction());
				}
				log.throwing(getClass().getName(), "doInTransaction", e);
				throw e;
			} catch (Exception e) {
				if (myTransaction) {
					log.fine("Rollback Connection for Thread " + Thread.currentThread().getName());
					rollback(con.getTransaction());
				}
				NikemJdbcException ex = new NikemJdbcException(e);
				log.throwing(getClass().getName(), "doInTransaction", ex);
				throw ex;
			}
			return result;
		}

		public static void rollback(EntityTransaction con) {
			if (con != null) {
				try {
					con.rollback();
				} catch (PersistenceException e) {
					log.log(Level.SEVERE, "Connection cannot be rolled back.", e);
				}
			}
		}
	}

	private static final Logger log = Logger.getLogger(JpaUtil.class.getName());

	private static final ThreadLocal<EntityManagerInfo> threadConnection = new ThreadLocal<EntityManagerInfo>() {
		protected EntityManagerInfo initialValue() {
			return null;
		};
	};

	private final String persistenceUnitName;
	private EntityManagerFactory entityManagerFactory;

	public JpaUtil(String persistenceUnitName) {
		super();
		this.persistenceUnitName = persistenceUnitName;
	}

	/**
	 * Execute a piece of work. The <code>Connection</code> object is provided to the {@link Work#apply(Object)} method.
	 * 
	 * @param <T> Type of the result
	 * @param work
	 *            piece of work to be executed
	 * @return result of the work execution
	 */
	public <T> T doWithoutTransaction(Work<EntityManager, T> work) throws NikemJdbcException {
		EntityManagerInfo info = threadConnection.get();
		EntityManager em = null;
		boolean myEm = false;
		T result;
		try {
			if (info == null) {
				log.fine("Retrieve EntityManagerInfo for Thread " + Thread.currentThread().getName());
				em = getEntityManager();
				info = new EntityManagerInfo(em);
				threadConnection.set(info);
				myEm = true;
			} else {
				log.fine("Reuse EntityManagerInfo for Thread " + Thread.currentThread().getName());
				em = info.getEm();
			}

			result = work.apply(em);

		} catch (RuntimeException e) {
			log.throwing(getClass().getName(), "doWithoutTransaction", e);
			throw e;
		} catch (Exception e) {
			NikemJdbcException ex = new NikemJdbcException(e);
			log.throwing(getClass().getName(), "doWithoutTransaction", ex);
			throw ex;
		} finally {
			if (myEm) {
				log.fine("Close EntityManager for Thread " + Thread.currentThread().getName());
				threadConnection.remove();
				close(em);
			}
		}
		return result;
	}

	public <T> T doInTransaction(Work<EntityManager, T> work) throws NikemJdbcException {
		return doWithoutTransaction(new TransactionWork<T>(work));
	}

	protected EntityManager getEntityManager() throws SQLException {
		return doGetEntityManager();
	}

	protected EntityManager doGetEntityManager() {
		return getEntityManagerFactory().createEntityManager();
	}

	/**
	 * Close EntityManager safely without throwing any exception.
	 * 
	 * @param em
	 *            the EntityManager to close (may be <code>null</code>)
	 */
	public static void close(EntityManager em) {
		if (em != null) {
			try {
				em.close();
			} catch (IllegalStateException e) {
				log.log(Level.SEVERE, "EntityManager cannot be closed.", e);
			}
		}
	}

	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	public EntityManagerFactory getEntityManagerFactory() {
		if (entityManagerFactory == null) {
			entityManagerFactory = Persistence.createEntityManagerFactory(getPersistenceUnitName());
		}
		return entityManagerFactory;
	}

	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	/**
	 * Helper method to avoid {@link javax.persistence.NoResultException}. If the collection is
	 * empty, null is returned, else the first element is returned.
	 * @param collection
	 * @return
	 */
	public <T> T getSingle(Collection<T> collection) {
		int size = collection.size();
		switch(size) {
		case 0: return null;
		case 1: return collection.iterator().next();
		default:throw new NonUniqueResultException("expected 0 or 1 result. Got " + size);
		}
	}
}
